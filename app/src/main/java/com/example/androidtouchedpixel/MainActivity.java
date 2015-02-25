package com.example.androidtouchedpixel;


import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.widget.Toast;



import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.os.Handler;


public class MainActivity extends Activity {
	
	TextView touchedXY, invertedXY, imgSize, colorRGB;
	ImageView imgSource1, imgSource2;
    TextToSpeech ttobj;

    String oldColorName = "";
    String blockText= "";

    private final int duration = 1; // seconds
    private final int sampleRate = 8000;
    private final int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 400; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    Handler handler = new Handler();


    @Override
    protected void onResume() {
        super.onResume();

        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
            }
        });
        thread.start();
    }
    void genTone(){
        // fill out the array
        for (int i = 0; i < numSamples/20; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    void playSound(){
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }


    @Override
    public void onPause(){
        if(ttobj !=null){
            ttobj.stop();
            ttobj.shutdown();
        }
        super.onPause();
    }

    public void speakText(View view){
        String toSpeak = blockText;//colorRGB.getTextColors().toString(); //write.getText().toString();

        //Uncomment to show text of TTS

        //Toast.makeText(getApplicationContext(), toSpeak,
        //        Toast.LENGTH_SHORT).show();
        ttobj.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);

    }

    public boolean colorCheck(String oldColor, String newColor){
        return !oldColor.equals(newColor);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        //touchedXY = (TextView)findViewById(R.id.xy);
        //invertedXY = (TextView)findViewById(R.id.invertedxy);
        //imgSize = (TextView)findViewById(R.id.size);

        //colorRGB = (TextView)findViewById(R.id.colorrgb);

    	//imgSource1 = (ImageView)findViewById(R.id.source1);
    	imgSource2 = (ImageView)findViewById(R.id.source2);
    	//imgSource1.setOnTouchListener(imgSourceOnTouchListener);
    	imgSource2.setOnTouchListener(imgSourceOnTouchListener);
        //write = (TextView)findViewById(R.id.colorrgb);
        //colorName = (TextView)findViewById(R.id.colorname);
        //color_name.setText("");


        ttobj=new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR){
                            ttobj.setLanguage(Locale.US);
                        }
                    }
                });
    	
    }

    OnTouchListener imgSourceOnTouchListener
    = new OnTouchListener(){

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			float eventX = event.getX();
			float eventY = event.getY();
			float[] eventXY = new float[] {eventX, eventY};

			Matrix invertMatrix = new Matrix();
			((ImageView)view).getImageMatrix().invert(invertMatrix);

			invertMatrix.mapPoints(eventXY);
			int x = Integer.valueOf((int)eventXY[0]);
			int y = Integer.valueOf((int)eventXY[1]);

			/*touchedXY.setText(
					"touched position: "
					+ String.valueOf(eventX) + " / "
					+ String.valueOf(eventY));
			invertedXY.setText(
					"touched position: "
					+ String.valueOf(x) + " / "
					+ String.valueOf(y));
    */
			Drawable imgDrawable = ((ImageView)view).getDrawable();
			Bitmap bitmap = ((BitmapDrawable)imgDrawable).getBitmap();

/*			imgSize.setText(
					"drawable size: "
					+ String.valueOf(bitmap.getWidth()) + " / "
					+ String.valueOf(bitmap.getHeight()));
*/
			//Limit x, y range within bitmap
			if(x < 0){
				x = 0;
			}else if(x > bitmap.getWidth()-1){
				x = bitmap.getWidth()-1;
			}

			if(y < 0){
				y = 0;
			}else if(y > bitmap.getHeight()-1){
				y = bitmap.getHeight()-1;
			}

			int touchedRGB = bitmap.getPixel(x, y);

            //Continent Map

            if(touchedRGB == Color.WHITE){
                //colorName.setText("white");

                blockText = "Ocean";
               /* colorRGB.setText("touched color: WHITE");
                colorRGB.setTextColor(Color.BLACK);
                */
            }

            else if (touchedRGB == Color.parseColor("#ff00cc00")){
                blockText = "North America";

            }
            else if (touchedRGB == Color.parseColor("#ff008000")){
                blockText = "South America";

            }
            else if (touchedRGB == Color.parseColor("#fffed52e")){
                blockText = "Africa";

            }
            else if (touchedRGB == Color.parseColor("#ffc10000")){
                blockText = "Europe";
            }
            else if (touchedRGB == Color.parseColor("#fff33e01")){
                blockText = "Asia";

            }
            else if (touchedRGB == Color.parseColor("#ff0040ff")){
                blockText = "Antarctica";

            }

            else if (touchedRGB == Color.parseColor("#ffc04080")){
                blockText = "Australia";
            }


            //Uncomment to Display Color (Uncomment colorRGB in XML and in initialization above
/*
            else{
                colorRGB.setText("touched color: " + "#" + Integer.toHexString(touchedRGB));
                colorRGB.setTextColor(touchedRGB);
            }
*/


            //If finger raised reset TTS
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                blockText = "";
            }

            //Keeps from continually repeating when left on same section
            if(colorCheck(oldColorName, blockText)) {
                speakText(view);
                oldColorName = blockText;
                //colorRGB.setText("True");
            }


			return true;

		}};




}
