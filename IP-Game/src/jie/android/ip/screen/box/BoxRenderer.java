package jie.android.ip.screen.box;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Expo;
import jie.android.ip.screen.actor.ImageActor;
import jie.android.ip.screen.actor.ImageActorAccessor;
import jie.android.ip.screen.box.BoxManager.Block;
import jie.android.ip.screen.box.BoxManager.OnRenderTweenListener;
import jie.android.ip.screen.box.BoxManager.Tray;

public class BoxRenderer {

	private final BoxConfig config;
	
	private ImageActor tray;
	
	public BoxRenderer(final BoxConfig config) {
		this.config = config;
	}

	private float colToBlockX(int col) {
		return config.getColBase() + col * (config.getBlockWidth() + config.getColSpace());
	}
	
	private float rowToBlockY(int row) {
		return config.getRowBase() + row * (config.getBlockHeight() + config.getRowSpace());
	}
	
	private float colToTrayX(int col) {
		return 0.0f;
	}
	
	public void putSourceBlock(int row, int col, final Block block) {
		final String name = String.format("s.%d.%d", col, row);
		final ImageActor actor = makeActor(name, block.style, block.status);
//		actor.setPosition(config.getRowBase() + row * config.getBlockHeight() + , col + 100);
		actor.setPosition(config.getColBase() + col * (config.getBlockWidth() + config.getColSpace())
				, config.getRowBase() + row * (config.getBlockHeight() + config.getRowSpace()));
		config.getSourceGroup().addActor(actor);
	}

	public void putTargetBlock(int row, int col, final Block block) {
		// TODO Auto-generated method stub
		
	}

	public void putTray(final Tray tray) {
		// TODO Auto-generated method stub
		
	}
		
	private final ImageActor makeActor(final String name, int style, int status) {
		return new ImageActor(name, config.getResources().getSkin().getRegion("ic"));
	}

	public void moveBlock(final int srow, final int scol, final int trow, final int tcol, final OnRenderTweenListener onTweenListener) {
		final String name = String.format("s.%d.%d", srow, scol);
		final ImageActor actor = (ImageActor) config.getSourceGroup().findActor(name);
		if (actor != null) {
			float tx = config.getColBase() + tcol * (config.getBlockWidth() + config.getColSpace());
			float ty = config.getRowBase() + trow * (config.getBlockHeight() + config.getRowSpace());
			Tween.to(actor, ImageActorAccessor.POSITION_Y, 0.2f).target(ty).ease(Expo.OUT).setCallback(new TweenCallback() {
				@Override
				public void onEvent(int type, BaseTween<?> source) {
					onTweenListener.onCompleted(false, srow, scol, trow, tcol);
				}
			}).start(config.getTweenManager());
		}
	}

	public void moveTrayWithBlock(final int scol, final int tcol, final OnRenderTweenListener onTweenListener) {
		final String name = String.format("s.%d.%d", 0, scol);
		final ImageActor actor = (ImageActor) config.getSourceGroup().findActor(name);
		if (actor != null) {
			float tbx = colToBlockX(tcol);
			float ttx = colToTrayX(tcol);
			Timeline.createParallel()
				.push(Tween.to(actor, ImageActorAccessor.POSITION_Y, 0.2f).target(tbx))
				.push(Tween.to(tray, ImageActorAccessor.POSITION_X, 0.2f).target(ttx))
			.setCallback(new TweenCallback() {

				@Override
				public void onEvent(int type, BaseTween<?> source) {
					onTweenListener.onCompleted(true, 0, scol, 0, tcol);
				}
			})
			.start(config.getTweenManager());
		}		
	}

	public void moveTray(final int scol, final int tcol, final OnRenderTweenListener onTweenListener) {
		float ttx = colToTrayX(tcol);
		Tween.to(tray, ImageActorAccessor.POSITION_X, 0.2f).target(ttx).setCallback(new TweenCallback() {

			@Override
			public void onEvent(int type, BaseTween<?> source) {
				onTweenListener.onCompleted(true, 0, scol, 0, tcol);
			}
		}).start(config.getTweenManager());
	}
	
	



	
	

}
