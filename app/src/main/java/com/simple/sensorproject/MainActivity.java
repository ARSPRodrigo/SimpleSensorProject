package com.simple.sensorproject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    private SensorManager sensorManager;
    private TextView view;
    private double lastUpdate;
    private Map<Double,Double> dataArray;
    private Queue<String> queue;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        view = findViewById( R.id.textView );

        sensorManager = ( SensorManager ) getSystemService( SENSOR_SERVICE );
        lastUpdate = System.currentTimeMillis();
        dataArray = new HashMap<>();
        queue = new ArrayDeque<>( 10 );
    }

    @Override
    public void onSensorChanged( SensorEvent event )
    {
        if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER )
        {
            getAccelerometer( event );
        }

    }

    private void getAccelerometer( SensorEvent event )
    {
        float[] values = event.values;
        double x = values[0];
        double actualTime = event.timestamp; // time in nano
        if( x > 1 )
        {
            dataArray.put( actualTime, x );
            lastUpdate = actualTime;
            if( dataArray.size() > 1 )
            {
                double totalAcc = 0;
                for( Double value : dataArray.values() )
                {
                    totalAcc += value;
                }
                double avgAcc = totalAcc / dataArray.size();
                double prevKey = -1;
                double totalTime = 0;
                for( Double key : dataArray.keySet() )
                {
                    if( prevKey != -1 )
                    {
                        totalTime += ( key - prevKey ) / 1000000.0; // time in milli secs
                    }
                    prevKey = key;
                }
                double avgTime = totalTime / dataArray.size();
                double avgVelocity = avgAcc * ( avgTime / 1000.0 ); // acceleration * time in secs
                display( avgAcc, avgTime, avgVelocity );
            }
        }
    }

    private void display( double avgAcc, double avgTime, double avgVelocity )
    {
        String data = "Average Acceleration: " + Math.round( avgAcc * 100 ) / 100.0 + " m/s^2"
                      + "\nAverage Time Interval: " + Math.round( avgTime * 100 ) / 100.0 + " ms"
                      + "\nAverage Velocity: " + Math.round( avgVelocity * 100 ) / 100.0 + " m/s\n";
        if( queue.size() == 10 )
        {
            queue.poll();
        }
        queue.add( data );
        view.setText( "Calculations\n" );
        for( String s : queue )
        {
            view.append( s );
        }
    }

    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy )
    {

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        sensorManager.registerListener( this,
                sensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER ),
                SensorManager.SENSOR_DELAY_NORMAL );
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener( this );
    }
}