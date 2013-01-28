package me.jromero.simpleandroidtunnel;

import java.io.IOException;

import me.jromero.lib.simpleandroidtunnel.LocalPortForwardingConfig;
import me.jromero.lib.simpleandroidtunnel.LocalPortForwardingTask.LocalPortForwardingListener;

import org.holoeverywhere.preference.EditTextPreference;
import org.holoeverywhere.preference.Preference;
import org.holoeverywhere.preference.Preference.OnPreferenceChangeListener;
import org.holoeverywhere.preference.PreferenceFragment;
import org.holoeverywhere.preference.SharedPreferences;
import org.holoeverywhere.widget.Toast;

import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.LocalPortForwarder;

public class LocalPortForwardingFragment extends PreferenceFragment implements
        OnPreferenceChangeListener {

    public static final String GATEWAY_HOST = "gateway-host";
    public static final String GATEWAY_PORT = "gateway-port";
    public static final String GATEWAY_USER = "gateway-user";
    public static final String GATEWAY_PASSWORD = "gateway-password";
    public static final String LOCAL_PORT = "local-port";
    public static final String REMOTE_HOST = "remote-host";
    public static final String REMOTE_PORT = "remote-port";
    private EditTextPreference mPrefGatewayHost;
    private EditTextPreference mPrefGatewayPort;
    private EditTextPreference mPrefGatewayUser;
    private EditTextPreference mPrefGatewayPassword;
    private EditTextPreference mPrefLocalPort;
    private EditTextPreference mPrefRemoteHost;
    private EditTextPreference mPrefRemotePort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.tunnel_configuration);

        SharedPreferences prefs = getDefaultSharedPreferences();

        mPrefGatewayHost = (EditTextPreference) findPreference(GATEWAY_HOST);
        mPrefGatewayHost.setOnPreferenceChangeListener(this);
        mPrefGatewayHost.setSummary(prefs.getString(GATEWAY_HOST, ""));

        mPrefGatewayPort = (EditTextPreference) findPreference(GATEWAY_PORT);
        mPrefGatewayPort.setOnPreferenceChangeListener(this);
        mPrefGatewayPort.setSummary(prefs.getString(GATEWAY_PORT, ""));

        mPrefGatewayUser = (EditTextPreference) findPreference(GATEWAY_USER);
        mPrefGatewayUser.setOnPreferenceChangeListener(this);
        mPrefGatewayUser.setSummary(prefs.getString(GATEWAY_USER, ""));

        mPrefGatewayPassword = (EditTextPreference) findPreference(GATEWAY_PASSWORD);
        mPrefGatewayPassword.setOnPreferenceChangeListener(this);
        mPrefGatewayPassword.setSummary(prefs.getString(GATEWAY_PASSWORD, ""));

        mPrefLocalPort = (EditTextPreference) findPreference(LOCAL_PORT);
        mPrefLocalPort.setOnPreferenceChangeListener(this);
        mPrefLocalPort.setSummary(prefs.getString(LOCAL_PORT, ""));

        mPrefRemoteHost = (EditTextPreference) findPreference(REMOTE_HOST);
        mPrefRemoteHost.setOnPreferenceChangeListener(this);
        mPrefRemoteHost.setSummary(prefs.getString(REMOTE_HOST, ""));

        mPrefRemotePort = (EditTextPreference) findPreference(REMOTE_PORT);
        mPrefRemotePort.setOnPreferenceChangeListener(this);
        mPrefRemotePort.setSummary(prefs.getString(REMOTE_PORT, ""));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // remove previous options
        // the activity will reset them once taken back
        menu.clear();

        // add our options
        inflater.inflate(R.menu.connections, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.connect:

            String gatewayHost = mPrefGatewayHost.getText();
            int gatewayPort = Integer.parseInt(mPrefGatewayPort.getText());
            String gatewayUser = mPrefGatewayUser.getText();
            String gatewayPassword = mPrefGatewayPassword.getText();

            int localPort = Integer.parseInt(mPrefLocalPort.getText());
            String remoteHost = mPrefRemoteHost.getText();
            int remotePort = Integer.parseInt(mPrefRemotePort.getText());

            LocalPortForwardingConfig config = new LocalPortForwardingConfig(
                    gatewayHost, gatewayPort, gatewayUser, gatewayPassword,
                    localPort, remoteHost, remotePort);

            config.setAuthenticationType(LocalPortForwardingConfig.AUTH_KEYBOARDINTERACTIVE);

            ((MainActivity) getActivity()).getTunnelService()
                    .localPortForwarding(config, mLocalPortForwardingListener);
            return true;
        case R.id.disconnect:
            if (mLocalPortForwarder != null) {
                try {
                    mLocalPortForwarder.close();
                    mLocalPortForwardingConnection.close();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private LocalPortForwarder mLocalPortForwarder;
    private Connection mLocalPortForwardingConnection;

    public LocalPortForwardingListener mLocalPortForwardingListener = new LocalPortForwardingListener() {

        @Override
        public void connectionLost(Throwable e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onException(Throwable e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT)
                    .show();

            e.printStackTrace();
        }

        @Override
        public void onEstablished(LocalPortForwarder forwarder) {
            Toast.makeText(getActivity(), "Established!!!", Toast.LENGTH_SHORT)
                    .show();

            mLocalPortForwarder = forwarder;
        }

        @Override
        public void onConnecting() {
            Toast.makeText(getActivity(), "Connecting...", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onConnected(Connection connection) {
            Toast.makeText(getActivity(), "Connected!", Toast.LENGTH_SHORT)
                    .show();
            mLocalPortForwardingConnection = connection;
        }

        @Override
        public void onAuthenticating() {
            Toast.makeText(getActivity(), "Authenticating...",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticated() {
            Toast.makeText(getActivity(), "Authenticated!", Toast.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key.contains("port")) {
            try {
                Integer.parseInt((String) newValue);

                preference.setSummary((String) newValue);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), "Ports must be only numbers",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            preference.setSummary((String) newValue);
        }
        return true;
    }
}
