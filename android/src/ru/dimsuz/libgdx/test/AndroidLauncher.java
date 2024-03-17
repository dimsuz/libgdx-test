package ru.dimsuz.libgdx.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import ru.dimsuz.libgdx.test.MyGdxGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		View renderView = initializeForView(new MyGdxGame(), config);
		FrameLayout layout = findViewById(R.id.frame);
		layout.addView(renderView, 0);

		findViewById(R.id.button).setOnClickListener(view -> {
			((Button)view).setText("Clicked!");
			getHandler().postDelayed(() -> ((Button)view).setText("Button"), 800);
		});
		findViewById(R.id.smol_button).setOnClickListener(view -> {
			((Button)view).setText("Clicked!");
			getHandler().postDelayed(() -> ((Button)view).setText("Button"), 800);
		});
	}
}
