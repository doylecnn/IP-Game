package jie.android.ip.screen.menu;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

import jie.android.ip.CommonConsts.ScreenConfig;
import jie.android.ip.common.actor.BaseGroup;
import jie.android.ip.common.actor.LabelActor;
import jie.android.ip.screen.menu.MenuRenderer.ItemClickListener;

public class PackGroup extends BaseGroup {

	private class ItemActor extends BaseGroup {

		private final Label label;
		
		public ItemActor(final String title, int all, int pass) {
			label = new Label(title, labelStyle);
			label.setFontScale(2.0f);
			
			this.addActor(label);
		}
		
		@Override
		protected void initStage() {
			
		}
		
	}
	
	private final MenuScreen screen;
	private final ItemClickListener clickListener;
	
	private final LabelStyle labelStyle;
	
	public PackGroup(final MenuScreen screen, final ItemClickListener packClickListener) {
		this.screen = screen;
		this.clickListener = packClickListener;
		
		this.labelStyle = new LabelStyle(this.screen.getGame().getResources().getBitmapFont(24), Color.WHITE);
		
//		BitmapFont font = this.screen.getGame().getResources().getBitmapFont(24);
//		font.draw(this.screen.getGame().getSpriteBatch(), "TEST", 300, 300);
		
		this.setBounds(0, 0, ScreenConfig.WIDTH, ScreenConfig.HEIGHT);
		
		this.screen.addActor(this);
	}

	@Override
	protected void initStage() {		
	}
	
	public void load(final ArrayList<Pack> packs) {
		LabelActor actor1 = new LabelActor("Pack-1", this.screen.getGame().getResources().getBitmapFont(24));
		this.addActor(actor1);
		actor1.setPosition(410, 210);
		actor1.setScale(2.0f);
		
//		
		for (final Pack pack : packs) {
			ItemActor actor = new ItemActor(pack.getTitle(), pack.getTotal_all(), pack.getTotal_pass());
			actor.setBounds(100, pack.getId() * 100, ScreenConfig.WIDTH, ScreenConfig.HEIGHT);
			this.addActor(actor);
		}
	}
	
	

}
