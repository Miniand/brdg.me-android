package com.miniand.brdgme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AuthActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Brdgme.getAuthToken().isEmpty()) {
            goHome();
            return;
        }
        AuthFragment fragment = new AuthFragment();
        fragment.setAuthFinishedHandler(new AuthFragment.AuthFinishedHandler() {
            @Override
            public void handleAuthFinished(String email, String token) {
                Brdgme.storeAuth(email, token);
                goHome();
            }
        });
        setContentView(R.layout.activity_auth);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AuthFragment extends Fragment {
        public enum State {
            ENTER_EMAIL, ENTER_CONFIRMATION, PROGRESS
        }

        private State state = State.ENTER_EMAIL;

        private AuthFinishedHandler authFinishedHandler;

        public AuthFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_auth, container, false);

            rootView.findViewById(R.id.log_in_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logIn();
                }
            });

            EditText authEmail = (EditText) rootView.findViewById(R.id.auth_email);
            authEmail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (state != State.ENTER_EMAIL) {
                        goToState(State.ENTER_EMAIL);
                    }
                }
            });

            return rootView;
        }

        public void logIn() {
            EditText authEmail;
            String email;
            View rootView = getView();
            if (rootView == null) {
                return;
            }
            authEmail = (EditText) rootView.findViewById(R.id.auth_email);
            email = authEmail.getText().toString();
            switch (state) {
                case ENTER_EMAIL:
                    if (TextUtils.isEmpty(email) ||
                            !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(getActivity(), "Please enter a valid email", Toast.LENGTH_LONG).show();
                        break;
                    }
                    requestAuth(email);
                    break;
                case ENTER_CONFIRMATION:
                    EditText authConfirmation = (EditText) rootView.findViewById(R.id.auth_confirmation);
                    String confirmation = authConfirmation.getText().toString();
                    if (TextUtils.isEmpty(confirmation)) {
                        Toast.makeText(getActivity(), "Please enter a confirmation code", Toast.LENGTH_LONG).show();
                        break;
                    }
                    confirmAuth(email, confirmation);
                    break;
            }
        }

        public void goToState(State state) {
            this.state = state;
            updateFromState();
        }

        private void updateFromState() {
            View rootView = getView();
            if (rootView == null) {
                return;
            }
            switch (state) {
                case ENTER_EMAIL:
                    rootView.findViewById(R.id.auth_explanation).setEnabled(true);
                    rootView.findViewById(R.id.auth_email).setEnabled(true);
                    rootView.findViewById(R.id.log_in_button).setEnabled(true);
                    rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    rootView.findViewById(R.id.auth_confirmation_message).setVisibility(View.GONE);
                    rootView.findViewById(R.id.auth_confirmation).setVisibility(View.GONE);
                    break;
                case ENTER_CONFIRMATION:
                    rootView.findViewById(R.id.auth_explanation).setEnabled(false);
                    rootView.findViewById(R.id.auth_email).setEnabled(true);
                    rootView.findViewById(R.id.log_in_button).setEnabled(true);
                    rootView.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    rootView.findViewById(R.id.auth_confirmation_message).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.auth_confirmation).setVisibility(View.VISIBLE);
                    break;
                case PROGRESS:
                    rootView.findViewById(R.id.auth_explanation).setEnabled(false);
                    rootView.findViewById(R.id.auth_email).setEnabled(false);
                    rootView.findViewById(R.id.log_in_button).setEnabled(false);
                    rootView.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.auth_confirmation_message).setVisibility(View.GONE);
                    rootView.findViewById(R.id.auth_confirmation).setVisibility(View.GONE);
                    break;
            }
        }

        private void requestAuth(final String email) {
            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    "http://api.brdg.me/auth/request",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            goToState(State.ENTER_CONFIRMATION);
                            focusEditText(R.id.auth_confirmation);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            goToState(State.ENTER_EMAIL);
                            Toast.makeText(
                                    getActivity(),
                                    "Unable to log in at the moment, please try again later.",
                                    Toast.LENGTH_LONG
                            ).show();
                            focusEditText(R.id.auth_confirmation);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    return params;
                }
            };
            Brdgme.getRequestQueue().add(request);
            goToState(State.PROGRESS);
        }

        private void focusEditText(int id) {
            View view = getView();
            if (view == null) {
                return;
            }
            if (view.findViewById(id).requestFocus()) {
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        private void confirmAuth(final String email, final String confirmation) {
            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    "http://api.brdg.me/auth/confirm",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Pattern p = Pattern.compile("^\"(.+)\"$");
                            Matcher m = p.matcher(response);
                            if (!m.find()) {
                                Toast.makeText(
                                        getActivity(),
                                        "Unable to confirm your code at the moment, please try again later.",
                                        Toast.LENGTH_LONG
                                ).show();
                                return;
                            }
                            finishAuth(email, m.group(1));
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            goToState(State.ENTER_CONFIRMATION);
                            Toast.makeText(
                                    getActivity(),
                                    "Please double check your confirmation code and try again.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("confirmation", confirmation);
                    return params;
                }
            };
            Brdgme.getRequestQueue().add(request);
            goToState(State.PROGRESS);
        }

        private void finishAuth(String email, String token) {
            if (authFinishedHandler != null) {
                authFinishedHandler.handleAuthFinished(email, token);
            }
        }

        public interface AuthFinishedHandler {
            void handleAuthFinished(String email, String token);
        }

        public void setAuthFinishedHandler(AuthFinishedHandler authFinishedHandler) {
            this.authFinishedHandler = authFinishedHandler;
        }
    }
}
