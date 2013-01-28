package me.jromero.simpleandroidtunnel;

import me.jromero.lib.simpleandroidtunnel.TunnelService;

import org.holoeverywhere.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private TunnelService mTunnelService;

    /**
     * Class for interacting with the main interface of the service.
     */
    public ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            TunnelService.ServiceBinder binder = (TunnelService.ServiceBinder) service;

            // Save service instance
            setTunnelService(binder.getService());
            Log.d(TAG, "Attached to service!");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            setTunnelService(null);
            Log.d(TAG, "Dettached from service!");
        }
    };

    public TunnelService getTunnelService() {
        return mTunnelService;
    }

    public void setTunnelService(TunnelService tunnelService) {
        mTunnelService = tunnelService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LocalPortForwardingFragment fragment = new LocalPortForwardingFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.root, fragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Binding to service...");

        Intent serviceIntent = new Intent(getApplicationContext(),
                TunnelService.class);

        startService(serviceIntent);

        if (!bindService(serviceIntent, mServiceConnection,
                Context.BIND_NOT_FOREGROUND)) {
            Log.e(TAG, "Failed to bind to service!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getApplicationContext(), TunnelService.class));
    }

}
