package xyy.scaleview;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import xyy.scaleview.scaleview.HorizontalScaleView;
import xyy.scaleview.scaleview.OnValueChangeListener;
import xyy.scaleview.scaleview.VerticalScaleView;

public class MainActivity extends Activity implements View.OnClickListener {

    private VerticalScaleView verticalScaleView;
    private HorizontalScaleView horizontalScaleView;
    private TextView heightTv;
    private TextView weightTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verticalScaleView = (VerticalScaleView) findViewById(R.id.vertical_scale);
        horizontalScaleView = (HorizontalScaleView) findViewById(R.id
                .horizontal_scale);
        heightTv = (TextView) findViewById(R.id.height);
        weightTv = (TextView) findViewById(R.id.weight);
        heightTv.setOnClickListener(this);
        weightTv.setOnClickListener(this);

        verticalScaleView.setRange(100, 200);
        horizontalScaleView.setRange(40, 100);

        verticalScaleView.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public void onValueChanged(float value) {
                heightTv.setText(String.valueOf(value));
                heightTv.setTextColor(getResources().getColor(R.color.main_color));
            }
        });
        horizontalScaleView.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public void onValueChanged(float value) {
                weightTv.setText(String.valueOf(value));
                weightTv.setTextColor(getResources().getColor(R.color.main_color));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.height:
                verticalScaleView.setVisibility(View.VISIBLE);
                horizontalScaleView.setVisibility(View.GONE);
                break;
            case R.id.weight:
                verticalScaleView.setVisibility(View.GONE);
                horizontalScaleView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
}