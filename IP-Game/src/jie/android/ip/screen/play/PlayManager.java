package jie.android.ip.screen.play;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.badlogic.gdx.utils.Disposable;

import jie.android.ip.database.DBAccess;
import jie.android.ip.executor.CommandSet;
import jie.android.ip.executor.Script;
import jie.android.ip.executor.CommandConsts.CommandType;
import jie.android.ip.playservice.PlayServiceTracker;
import jie.android.ip.screen.play.Cmd.State;
import jie.android.ip.screen.play.Code.Lines;
import jie.android.ip.utils.Utils;

public class PlayManager implements Disposable {

	protected static final String Tag = PlayManager.class.getSimpleName();

	private final PlayScreen screen;
	private final DBAccess dbAccess;

	private int packId = -1;
	private Script script = null;
	private int script_status = 0;
	private int script_base_score = 0;
	private CommandSet cmdSet;
	private int execStep = 0;

	private PlayScreenListener.ManagerEventListener managerListener;

	private final Box box;;
	private final Code.Lines codeLines;
	private final PlayExecutor executor;
	
	private final PlayServiceTracker playServiceTracker = new PlayServiceTracker();

	private final PlayScreenListener.RendererEventListener rendererListener = new PlayScreenListener.RendererEventListener() {

		@Override
		public void onBoxMoveStart() {
		}

		@Override
		public void onBoxkMoveEnd() {

			if (!box.checkResult()) {
				executor.next();
			} else {
				executor.stop(PlayExecutor.StopReason.SUCC);
				// onExecuteEnd(true);
			}
		}

		@Override
		public void onPanelButtonClicked(int index, int pos, final Code.Type type) {
			codeLines.setNode(index, pos, type);
		}

		@Override
		public void onCmdButtonClicked(final Cmd.Type type, final Cmd.State state) {
			if (type == Cmd.Type.RUN) {
				onCmdRun(state);
			} else if (type == Cmd.Type.CLEAR) {
				onCmdClear(state);
			} else if (type == Cmd.Type.NEXT) {
				onCmdNext(state);
			} else if (type == Cmd.Type.CLOSE || type == Cmd.Type.CLOSE2) {
				onCmdClose(state);
			} else if (type == Cmd.Type.DEBUG) {
				onCmdDebug(state);
			} else if (type == Cmd.Type.DEBUG_OVER) {
				onCmdDebugOver(state);
			} else if (type == Cmd.Type.BACK || type == Cmd.Type.BACK2) {
				onCmdBack(state);
			}
		}

	};

	private final PlayScreenListener.ManagerInternalEventListener internalListener = new PlayScreenListener.ManagerInternalEventListener() {

		@Override
		public void onExecuteMove(boolean right) {
			box.tryMoveTray(right);
			
			playServiceTracker.update(right ? PlayServiceTracker.Type.MOVE_RIGHT : PlayServiceTracker.Type.MOVE_RIGHT);
		}

		@Override
		public void onExecuteCompleted(final PlayExecutor.StopReason reason) {
			Utils.log(Tag, "executeCompleted : " + reason);

			if (reason == PlayExecutor.StopReason.SUCC) {
				onExecuteSucc();
			} else if (reason == PlayExecutor.StopReason.RESET) {
				onExecuteReset();
				playServiceTracker.update(PlayServiceTracker.Type.EXECUTE_MAX_RESET);
			} else if (reason == PlayExecutor.StopReason.FINISHED) {
				onExecuteFinished();				
				playServiceTracker.update(PlayServiceTracker.Type.EXECUTE_MAX_FINISHED);
			} else if (reason == PlayExecutor.StopReason.EXCEPTION) {
				onExecuteException();
				playServiceTracker.update(PlayServiceTracker.Type.EXECUTE_MAX_EXCEPTION);
			} else if (reason == PlayExecutor.StopReason.OVERFLOW) {
				onExecuteOverflow();
				playServiceTracker.update(PlayServiceTracker.Type.EXECUTE_MAX_OVERFLOW);
			} else {
				Utils.log(Tag, "Unsupport execute stop reason - " + reason);
			}
			
			playServiceTracker.update(PlayServiceTracker.Type.EXECUTE_MIN_SUCC);
		}

		@Override
		public void onExecuteAction() {
			box.tryMoveBlock();
		}

		@Override
		public void onBoxLoadCompleted(final Box.Tray tray, final Box.BlockArray source, final Box.BlockArray target) {
			if (managerListener != null) {
				managerListener.onBoxLoadCompleted(tray, source, target);
			}
		}

		@Override
		public void onBoxPreReload(final Box.Tray tray, final Box.BlockArray source, final Box.BlockArray target) {
			if (managerListener != null) {
				managerListener.onBoxPreReload(tray, source, target);
			}
		}

		@Override
		public void onCodeLineLoadCompleted(final Code.Lines lines) {
			if (managerListener != null) {
				managerListener.onCodeLineLoadCompleted(lines);
			}
		}

		@Override
		public void onCodeLineUpdated(final Code.Lines lines, int index, int pos) {
			if (managerListener != null) {
				managerListener.onCodeLineUpdated(lines, index, pos);
			}
		}

		@Override
		public void onCodeLineResetCompleted(Lines lines) {
			if (managerListener != null) {
				managerListener.onCodeLineResetCompleted(lines);
			}
		}

		@Override
		public void onBoxMoved(final Box.Tray tray, final Box.Block block, int col, int row, int tcol, int trow) {

			if (tray == null && block != null) { // block moved
				if (row - trow > 0) { // down
					executor.setRTVariant(0, block.value);
					executor.setRTVariant(1, 1);
				} else {
					executor.clearRTVariant(0);
					executor.setRTVariant(1, 0);
				}
			} else {
				++ execStep;
				
				playServiceTracker.update(PlayServiceTracker.Type.STEP_MAX);
			}

			if (managerListener != null) {
				managerListener.onBoxMoved(tray, block, col, row, tcol, trow);
			}
		}

		@Override
		public void onBoxMoveEmpty() {
			playServiceTracker.update(PlayServiceTracker.Type.ACTION_EMPTY);
			
			if (managerListener != null) {
				managerListener.onBoxMoveEmpty();
			}
		}

		@Override
		public void onBoxMoveException(int error) {
			Utils.log(Tag, "onBoxMoveException : " + error);
			executor.stop(PlayExecutor.StopReason.EXCEPTION);
			
			// onExecuteEnd(false);
		}

		@Override
		public void onCodeCalled(int type, int func, int index) {
			if (type == CommandType.CALL.getId()) {
				playServiceTracker.update(PlayServiceTracker.Type.CALL_MAX);
			} else if (type == CommandType.CHECK.getId()) {
				playServiceTracker.update(PlayServiceTracker.Type.CHECK_MAX);
			} else if (type == CommandType.ACT.getId()) {
				playServiceTracker.update(PlayServiceTracker.Type.ACTION_MAX);
			}
			
			if (managerListener != null) {
				managerListener.onCodeCalled(type, func, index);
			}
		}

		@Override
		public boolean onExecutePause() {
			if (managerListener != null) {
				if (managerListener.onExecutePause()) {
					return true;
				}
			}
			return false;
		}
	};

	//

	public PlayManager(final PlayScreen screen) {
		this.screen = screen;
		this.dbAccess = this.screen.getGame().getDBAccess();

		this.box = new Box(internalListener);
		this.codeLines = new Code.Lines(internalListener);
		this.executor = new PlayExecutor(internalListener);
	}

	@Override
	public void dispose() {
		executor.dispose();
	}
	
	public boolean loadScript(final int packId, final int scriptId) {

		this.packId = packId;
		script = new Script(scriptId);

		final ResultSet rs = dbAccess.loadScript(scriptId);
		if (rs != null) {
			try {
				try {
					if (rs.next()) {
						final String str = rs.getString(1);
						if (str == null) {
							return false;
						}
						if (!script.loadString(str)) {
							return false;
						}
						script_status = rs.getInt(2);
						script_base_score = rs.getInt(3);
					} else {
						return false;
					}
				} finally {
					rs.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}

		final String str = dbAccess.loadSolution(scriptId);
		if (str != null) {
			cmdSet = CommandSet.loadFromString(str);
		}

		init();

		managerListener.onScriptLoaded(packId, scriptId, getPackTitle(packId), script.getSelfId(), script.getTitle(), script.getAuthor(), script.getComment());
		
		return true;
	}

	private final String getPackTitle(int id) {
		return dbAccess.getPackTitle(id);
	}

	public void setEventListener(final PlayScreenListener.ManagerEventListener listener) {
		this.managerListener = listener;
	}

	public final PlayScreenListener.RendererEventListener getRendererEventListener() {
		// TODO Auto-generated method stub
		return rendererListener;
	}

	private void init() {
		box.loadScript(script);
		codeLines.loadCmdSet(cmdSet);
	}

	protected void onCmdRun(final Cmd.State state) {
		if (state == Cmd.State.NONE) {
			executeScript();
		} else {
			stopScript();
		}
	}
	
	private void executeScript() {
		cmdSet = codeLines.makeCommandSet();
		final String cmd = cmdSet.saveToString();
		if (cmd != null) {
			dbAccess.saveSolution(script.getId(), cmd);
		} else {
			Utils.log(Tag, "ERROR! - solution command is NULL.");
		}
		execStep = 0;
		executor.setDelay((long)(screen.getClockSpeed() * 1000));
		executor.execute(cmdSet);		
	}
	
	private void stopScript() {
		box.reload(script);
		executor.reset();		
	}

	protected void onCmdClear(final Cmd.State state) {
		box.reload(script);
		executor.reset();

		dbAccess.clearSolution(script.getId());
		codeLines.reset();
	}

	protected void onCmdNext(final Cmd.State state) {
		screen.setNextScreen();
	}

	protected void onCmdClose(final Cmd.State state) {
		screen.returnMenuScreen();
	}
	
	protected void onCmdDebug(final Cmd.State state) {
		executor.stepOver();	
	}

	protected void onCmdDebugOver(final Cmd.State state) {
		executor.stepOver();		
	}	

	protected void onCmdBack(final Cmd.State state) {
		stopScript();
	}
	
	protected void onExecuteSucc() {
		int score = cmdSet.calcScore();
		dbAccess.updateScriptStatus(script.getId(), 1);
		
		if(dbAccess.updateSolutionScore(script.getId(), score) > 0) {
			this.screen.getGame().getPlayEventListener().onPackItemPlaySucc(packId, script.getId(), score);	
		}
		
		playServiceTracker.check(this.screen.getGame().getPlayEventListener());
		
		if (managerListener != null) {
			managerListener.onExecuteSucc(script_base_score, score, execStep);
		}
	}

	protected void onExecuteReset() {
		playServiceTracker.refresh(false);
	}

	protected void onExecuteFinished() {
		if (managerListener != null) {
			managerListener.onExecuteFinished();
		}
	}

	protected void onExecuteException() {
		if (managerListener != null) {
			managerListener.onExecuteFail();
		}

	}

	protected void onExecuteOverflow() {
		if (managerListener != null) {
			managerListener.onExecuteOverflow();
		}		
	}	
}
