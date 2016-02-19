package com.fenchtose.contactsdemo;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.fenchtose.fftpack.RealDoubleFFT;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


public class OaeTestFragment extends Fragment implements OnClickListener {
    /** Private variables for Recording Audio */
    int recordingSampleRate;                                // Sampling Rate
    int audioSource = MediaRecorder.AudioSource.MIC;        // Audio source is the device mic
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO; // recording in mono
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;     // recording in 16 bits
    private RealDoubleFFT transformer;                      // the fft double array
    int blockSize = 256;                                    // deal with this many samples at a time
    public double frequency = 0.0;// frequency recorded

    /** Private variables for Playing Tones */
    private final int toneSampleRate = 44000;
    private final int targetSamples = 5500;
    private int numSamples1 = 5500;      // calculated wrt to frequency later
    private int numCycles1 = 500;    // calculated wrt to frequency later
    private int numSamples2 = 5500;      // calculated wrt to frequency later
    private int numCycles2 = 500;    // calculated wrt to frequency later
    private double sample1[] = new double[targetSamples * 2];
    private byte generatedSnd1[] = new byte[2 * 2 * targetSamples];
    private double sample2[] = new double[targetSamples * 2];
    private byte generatedSnd2[] = new byte[2 * 2 * targetSamples];

    /** Private variables for Graphics */
    ToggleButton startStopRecord;                           // button for Recording
    ToggleButton startStopPlay1;                            // button for Playing Tone 1
    ToggleButton startStopPlay2;                            // button for Playing Tone 2
    SeekBar seekBar1;
    SeekBar seekBar2;
    TextView recordFreq;
    TextView toneFreq1;                                     // text view for string 1
    TextView toneFreq2;                                     // text view for string 2
    TextView recordDisplay;
    TextView freqDisplay1;                                  // text view to display tone 1
    TextView freqDisplay2;                                  // text view to display tone 2

    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    private RecyclerView rootView;

    /** Private boolean variables */
    boolean runningRecord = false;                          // used to control whether or not audio is being recorded
    boolean runningPlay1 = false;                           // used to control tone 1
    boolean runningPlay2 = false;
    private static final String TAG = "OAE";// used to control tone 2
    private static final int REQUEST_ENABLE_BT = 1;// Bluetooth protocol //TODO: Verify if this works
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // Well known SPP UUID
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Insert your server's MAC address
    private static String address = "00:18:A1:12:12:1E";
    public static OaeTestFragment newInstance() {
        return new OaeTestFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }//end onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        rootView = (RecyclerView)inflater
                .inflate(R.layout.oaetest, parent, false);
        btAdapter = BluetoothAdapter.getDefaultAdapter(); //TODO:check this
        checkBTState();//TODO:check this
        transformer = new RealDoubleFFT(blockSize);         //transformer of block size 256b
        imageView = (ImageView) rootView.findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap(1080, 400, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);

        startStopRecord = (ToggleButton) rootView.findViewById(R.id.startStopRecord);
        startStopRecord.setOnClickListener(this);
        startStopPlay1 = (ToggleButton) rootView.findViewById(R.id.startStopPlay1);
        startStopPlay1.setOnClickListener(this);
        startStopPlay2 = (ToggleButton) rootView.findViewById(R.id.startStopPlay2);
        startStopPlay2.setOnClickListener(this);
        seekBar1 = (SeekBar) rootView.findViewById(R.id.seekBar1);
        seekBar1.setOnSeekBarChangeListener(seekBarListener1);
        seekBar2 = (SeekBar) rootView.findViewById(R.id.seekBar2);
        seekBar2.setOnSeekBarChangeListener(seekBarListener2);
        recordFreq = (TextView) rootView.findViewById(R.id.recordFreq);
        toneFreq1 = (TextView) rootView.findViewById(R.id.toneFreq1);
        toneFreq2 = (TextView) rootView.findViewById(R.id.toneFreq2);
        recordDisplay = (TextView) rootView.findViewById(R.id.recordDisplay);
        freqDisplay1 = (TextView) rootView.findViewById(R.id.freqDisplay1);
        freqDisplay2 = (TextView) rootView.findViewById(R.id.freqDisplay2);

        seekBar1.setMax(124);
        seekBar1.setProgress(61);
        seekBar2.setMax(124);
        seekBar2.setProgress(61);

        return rootView;
    }


    public void onResume() {
        super.onResume();

        Log.d(TAG, "...In onResume - Attempting client connect...");

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.d("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting to Remote...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Log.d("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // kill the child thread
        runningRecord = false;
        runningPlay1 = false;
        runningPlay2 = false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings){
            Toast.makeText(getActivity(), "Settings", Toast.LENGTH_SHORT).show();

            //if it's being run in the background, stop it
            if (runningRecord) {
                runningRecord = false;
                startStopRecord.setText("Start");
            }//end if

            //startActivity(new Intent(getActivity(), SettingsActivity.class));
        }//end if

        return super.onOptionsItemSelected(item);
    }//end onOptionsItemSelected

    @Override
    /* On click, "started" is set to either start/stop by false/true */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startStopRecord:
                Toast.makeText(getActivity(), "Record", Toast.LENGTH_SHORT).show();
                if (runningRecord) {
                    runningRecord = false;
                    startStopRecord.setText("Start");
                } else {
                    runningRecord = true;
                    startStopRecord.setText("Stop");

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String stringSampleRate = prefs.getString(getString(R.string.pref_sampleRate_key), getString(R.string.pref_sampleRate_default));
                    recordingSampleRate = Integer.parseInt(stringSampleRate);

                    new RecordAudio().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //invoked on the UI thread, executes asynchronous task
                }//end else
                break;
            case R.id.startStopPlay1:
                runningPlay1 = !runningPlay1;
                playSound1();
                Toast.makeText(getActivity(), "Tone 1", Toast.LENGTH_SHORT).show();

                //kill any existing threads in case the button is being spammed


                break;
            case R.id.startStopPlay2:
                runningPlay2 = !runningPlay2;
                playSound2();
                Toast.makeText(getActivity(), "Tone 2", Toast.LENGTH_SHORT).show();

                //kill any existing threads in case the button is being spammed

                //check if light is off, if so, turn it on
                break;
        }
    }

    /**
     * A call-back for when the user moves the sine seek bars
     */
    OnSeekBarChangeListener seekBarListener1 = new OnSeekBarChangeListener() {

        public void onStopTrackingTouch(SeekBar seekBar) {
            // genTone1(convertProgress_Hz(seekBar1.getProgress()));
            if (seekBar1.getProgress()<37) {
                // makes a little message pop up
                Toast wmsg = Toast.makeText(getActivity(), "you can't hear <100Hz on a phone speaker", Toast.LENGTH_LONG);
                wmsg.setGravity(Gravity.TOP, wmsg.getXOffset() / 2, wmsg.getYOffset() / 2);
                wmsg.show();
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (seekBar1.getProgress()<1) seekBar1.setProgress(1);
            freqDisplay1.setText(Double.toString(convertProgress_Hz(seekBar1.getProgress())));

        }
    };

    /**
     * A call-back for when the user moves the sine seek bars
     */
    OnSeekBarChangeListener seekBarListener2 = new OnSeekBarChangeListener() {

        public void onStopTrackingTouch(SeekBar seekBar) {
            //genTone2(convertProgress_Hz(seekBar2.getProgress()));
            if (seekBar2.getProgress()<37) {
                // makes a little message pop up
                Toast wmsg = Toast.makeText(getActivity(), "you can't hear <100Hz on a phone speaker", Toast.LENGTH_LONG);
                wmsg.setGravity(Gravity.TOP, wmsg.getXOffset() / 2, wmsg.getYOffset() / 2);
                wmsg.show();
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (seekBar2.getProgress()<1) seekBar2.setProgress(1);
            freqDisplay2.setText(Double.toString(convertProgress_Hz(seekBar2.getProgress())));
        }
    };
    /***************************************************************************************************
     ************************************* Record Audio ************************************************
     **************************************************************************************************/

    /* RecordAudio is an asynchronous task, which continuously reads in the audio in the background. */
    public class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        /* This task is executed asynchronously from recordTask.execute() */
        protected Void doInBackground(Void...params) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(
                        recordingSampleRate, channelConfiguration, audioEncoding);        // minimum buffer size
                AudioRecord audioRecord = new AudioRecord(
                        audioSource, recordingSampleRate,
                        channelConfiguration, audioEncoding, bufferSize);           // AudioRecord object

                short[] buffer = new short[blockSize];                              // save the raw PCM samples as short bytes
                double[] toTransform = new double[blockSize];                       // of size of blockSize

                audioRecord.startRecording();                                       // starts receiving audio

                 /* Reads the data from the microphone. it takes in data
                   to the size of the window "blockSize". The data is then
                   given in to audioRecord. The int returned is the number
                   of bytes that were read */
                while (runningRecord) {
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);  // reads result from the AudioRecord Object

                    /* dividing the short by 32768.0 gives us the result in a range -1.0 to 1.0. */
                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0;              // signed 16 bit, adjusts result from the buffer[] array into the toTransform[] array
                    }//end for

                    transformer.ft(toTransform);                                    // transforms result in toTransforms
                    publishProgress(toTransform);                                   // publishProgress() sends toTransform to onProgressUpdate to update canvas
                }//end while

                audioRecord.stop();
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed");
            }//end catch

            return null;
        }//end doInBackground

        @Override
        /* Runs on the UI thread after publishProgress. Updates the canvas with the values from the
           recently transformed toTransform. */
        protected void onProgressUpdate(double[]... toTransform) {
            double[] re = new double[blockSize];                                // array used to store real parts
            double[] im = new double[blockSize];                                // array used to store imaginary parts
            double[] magnitude = new double[blockSize / 2];                     // used to calculate frequency by looking at the peak

            // Calculate the Real and imaginary and Magnitude.
            for(int i = 0; i < magnitude.length; i++){
                re[i] = toTransform[0][i*2];                                   // real is stored in first part of array
                im[i] = toTransform[0][(i*2)+1];                               // imaginary is stored in the sequential part
                magnitude[i] = Math.sqrt((re[i] * re[i]) + (im[i]*im[i]));     // magnitude is calculated by the square root of (imaginary^2 + real^2)
            }//end for

            int maxIndex = 0;                                                  // bin index
            double max = magnitude[0];
            for (int i = 1; i < magnitude.length; i++) {
                if (magnitude[i] > max){
                    max = magnitude[i];
                    maxIndex = i;
                }//end if
            }//end for

            frequency = (maxIndex * recordingSampleRate)/blockSize;                  // calculated the frequency
            String stringFreq = Double.toString(frequency);
            recordDisplay.setText(stringFreq);
            canvas.drawColor(Color.BLACK);

            //Draws a graph that is buffer result by frequency
            for (int i = 0; i < toTransform[0].length; i++) {
                // i = x
                int downy = (int) (400 - (toTransform[0][i] * 400));
                int upy = 400;

                canvas.drawLine(i, downy, i, upy, paint);
            }//end for
            imageView.invalidate();
        }//end onProgressUpdate
    }//end RecordAudio

    /***************************************************************************************************
     ************************************* Play Sound **************************************************
     **************************************************************************************************/
    // this runs the process in a background thread so the UI isn't locked up


    // Based on but modified and improved from
    // http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
    // functions for tone generation

    void playSound1(){
        /*
        final AudioTrack audioTrack1 = new AudioTrack(AudioManager.STREAM_MUSIC,
                toneSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples1*2,
                AudioTrack.MODE_STREAM);
        audioTrack1.write(generatedSnd1, 0, numSamples1*2);
        audioTrack1.play();
        while (runningPlay1==true){
            audioTrack1.write(generatedSnd1, 0, numSamples1*2);
        }
        audioTrack1.stop();
        runningPlay1 = false;
        */
        if(runningPlay2) {
            int tempfreq = seekBar1.getProgress();
            //TODO:get the value of the seek bar
            sendData("" + convertProgress_Hz(tempfreq));//TODO: Sends the frequency via bluetooth
            Log.i("itworks", TAG);
        }
    }

    void playSound2(){
        if(runningPlay2) {
            int tempfreq2 = seekBar2.getProgress(); //get the value of the seek bar
            sendData("" + convertProgress_Hz(tempfreq2));
            Log.i("itworks", TAG);
        }
    }


    //functions to convert progress bar into time and frequency
    private double convertProgress_Hz(int progress) {

        double Hz = 440;

        //http://www.phy.mtu.edu/~suits/NoteFreqCalcs.html
        // Java was bad at powers math of non integers, so made a loop to do the powers

        // A440 base pitch is adjusted down 5 octaves by multiplying by 2^(-60/12) = 0.03125
        Hz = (427.5 + 0.125 * (float) 100) * 0.03125;
        // Raise the base pitch to the 2^n/12 power
        for(int m=1; m<(progress); m++) {
            Hz = Hz * 1.0594630943593;  // 2^(1/12)
        }

        return (int)Hz;
    }


    private void checkBTState() {  //TODO:checks the status of bluetooth
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            Log.d(TAG,"No bluetooth adapter" );
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Sending data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
            msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            Log.d("Fatal Error", msg);
        }
    }

} //end FreqFragment


