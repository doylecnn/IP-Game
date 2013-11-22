package jie.android.ip.screen;

import aurelienribon.tweenengine.Tween;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import jie.android.ip.IPGame;
import jie.android.ip.CommonConsts.ScreenConfig;
import jie.android.ip.executor.Analyser;
import jie.android.ip.executor.CommandConsts.ActType;
import jie.android.ip.executor.CommandSet;
import jie.android.ip.executor.Executor;
import jie.android.ip.executor.OnCommandListener;
import jie.android.ip.group.BaseGroup;
import jie.android.ip.screen.actor.ImageActor;
import jie.android.ip.screen.actor.ImageActorAccessor;
import jie.android.ip.screen.actor.OnActorInputListener;
import jie.android.ip.screen.box.BoxRenderConfig;
import jie.android.ip.screen.box.BoxManager;
import jie.android.ip.screen.box.OnBoxEventListener;
import jie.android.ip.screen.box.BoxManager.Direction;
import jie.android.ip.script.Script;
import jie.android.ip.utils.Utils;

public class TestScreen extends BaseScreen {

	private ImageActor block;
	private Button btn;
	
	private Executor exe = new Executor();
	private BoxManager bmanager;
	
	private OnCommandListener cmdListener = new OnCommandListener() {

		@Override
		public void onStart() {
		}

		@Override
		public void onEnd() {
		}

		@Override
		public void onCall(int func, int index) {
		}

		@Override
		public void onAct(int func, int index, Object param1, Object param2) {
			int action = ((Integer)param1); 
			if (action == ActType.ACTION.getId()) {
				bmanager.doAction();
			} else if (action == ActType.MOVE_LEFT.getId()){
				bmanager.doMove(false);
			} else if (action == ActType.MOVE_RIGHT.getId()){
				bmanager.doMove(true);
			}
			//Tween.to(block, ImageActorAccessor.POSITION_X, 0.1f).target(block.getX() + 100).start(tweenManager);
			
//			block.setPosition(block.getX() + 100, block.getY());
		}

		@Override
		public void onCheck(int func, int index, Object param1, Object param2) {
		}

		@Override
		public void onBreakPoint(int func, int index, String cmd, Object param1, Object param2) {			
		}		
	};
	
	private OnBoxEventListener boxListener = new OnBoxEventListener() {

		@Override
		public void onTrayStatusChanged(boolean attached) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onBlockMoveStart(boolean down) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onBlockMoveEnd(boolean down) {
			bmanager.moveTray(Direction.RIGHT);
		}

		@Override
		public void onTrayMoveStart(boolean right) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTrayMoveEnd(boolean right, boolean succ) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
//	private OnActorInputListener listener;
	
	public TestScreen(IPGame game) {
		super(game);
		
//		listener = new OnActorInputListener() {
//
//			@Override
//			public void onTouchUp(Actor actor, float x, float y, int pointer, int button) {
//				Utils.log("test", "touchUp - x = " + x + " y = " + y);
//			}
//
//			@Override
//			public void onTouchDown(Actor actor, float x, float y, int pointer, int button) {
//				Utils.log("test", "touchDown - x = " + x + " y = " + y);				
//			}
//
//			@Override
//			public void onTouchDragged(Actor actor, float x, float y, int pointer) {
//				Utils.log("test", "touchDragged - x = " + x + " y = " + y);			
//			}
//			
//		};
//		initExecutor();
//		initGroups();
		initActors();
		
		
		initCmds();
		
		
		BaseGroup group = new BaseGroup();
		group.setBounds(0, 100, ScreenConfig.WIDTH, ScreenConfig.HEIGHT);
//		group.setBounds(200, 100, 100, 100);
//		group.setScale(0.5f);
		
		Script script = new Script();
		script.load("");
		
		BoxRenderConfig config = new BoxRenderConfig();
		config.setSourceGroup(group);
		config.setResources(game.getResources());
		config.setTweenManager(this.tweenManager);
		
		bmanager = new BoxManager(config);
		bmanager.loadScript(script);
		bmanager.setOnEventListener(boxListener);
		
//		bmanager.putSource(group);
		
		this.addActor(group);
	
//		Gdx.input.setInputProcessor(this);
	}

	private void initActors() {
		
//		block = new ImageActor("ic", game.getResources().getSkin().getRegion("ic"));
//		block.setPosition(0, 100);
//		this.addActor(block);
		
		btn = new Button(game.getResources().getSkin().getDrawable("ic"));
		btn.setBounds(0, 0, 64, 64);
		btn.addListener(new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				Utils.log("testscreen", "clicked");
				bmanager.moveBlock(2, BoxManager.Direction.DOWN);
				//run();
			}			
		});
		this.addActor(btn);
	}
	
	private void initCmds() {
		
	}
	
	private void run() {
		CommandSet cmdset = Analyser.makeCommandSet(".\\doc\\test.xml");

		exe.setDelay(100);
		exe.enableOneStep(true);
		exe.start(cmdset, cmdListener);		
	}
	

}
