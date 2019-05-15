package pe.huntlng.vehiclescan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;


public class HandheldFragment extends Fragment {

    private OnHandheldFragmentInteractionListener mListener;
    public static final String VEHICLE_MESSAGE = "com.huntloc.handheldvehiclecontrol.VEHICLE";
    public static final int SCAN_REQUEST = 1;  // The request code
    private static EditText editText_Plate;
    private Button button_Ckeck;
    private Button button_Scan;
    public HandheldFragment() {
    }


    public static HandheldFragment newInstance(String param1, String param2) {
        HandheldFragment fragment = new HandheldFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_handheld, container, false);
        editText_Plate = (EditText) view
                .findViewById(R.id.editText_Plate);
        editText_Plate.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (editText_Plate.getText().toString().isEmpty()) {
                        Toast.makeText(HandheldFragment.this.getActivity(),
                                "Enter a vehicle plate i.e. ABC123",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        sendRequest();
                    }
                    return true;
                }
                return false;
            }
        });

        button_Ckeck = (Button) view.findViewById(R.id.button_Check);
        button_Ckeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText_Plate.getText().toString().isEmpty()) {
                    Toast.makeText(HandheldFragment.this.getActivity(),
                            "Enter a vehicle plate i.e. ABC123",
                            Toast.LENGTH_SHORT).show();
                } else {

                    sendRequest();
                    hideKeyboard();
                }
            }
        });

        button_Scan = (Button) view.findViewById(R.id.button_Scan);
        button_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),
                        ScanActivity.class);
                getActivity().startActivityForResult(intent, SCAN_REQUEST);
            }
        });
        return view;
    }

    private void hideKeyboard() {
        View view = HandheldFragment.this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) HandheldFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void sendRequest() {
        String serverURL = getResources().getString(R.string.service_url)
                + "/VehicleService/Retrieve/ByPlate/" + editText_Plate.getText().toString();
        Log.d("URL vehicle", serverURL);
        new QueryVehicleTask(this).execute(serverURL);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHandheldFragmentInteractionListener) {
            mListener = (OnHandheldFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void setPlate(String plate){

        editText_Plate.setText(plate.replace("-",""));
        sendRequest();
    }

   /*protected void displayVehicle(String plate) {
        Intent intent = new Intent(getActivity(),
                VehicleActivity.class);
        intent.putExtra(VEHICLE_MESSAGE, plate);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        //
   }*/

    public interface OnHandheldFragmentInteractionListener {
        void onHandheldFragmentInteraction();
    }
    private class QueryVehicleTask extends AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        private WeakReference<HandheldFragment> handheldFragmentWeakReference;
        private QueryVehicleTask(HandheldFragment fragment) {
            this.handheldFragmentWeakReference = new WeakReference<HandheldFragment>(
                    fragment);
        }
        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                Log.d("Exception",e.getMessage());
                handheldFragmentWeakReference.get().getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(handheldFragmentWeakReference.get().getActivity());
                        alertDialogBuilder.setTitle("Vehicle Control");
                        alertDialogBuilder.setMessage("Red WiFi no Disponible");
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        alertDialogBuilder.create().show();
                    }
                });
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }
        protected void onPostExecute(String result) {
            Log.d("Result", result);
            try {
                if (result!=null && !result.equals("")) {
                    JSONObject jsonResponse = new JSONObject(result);
                    String plate = jsonResponse.optString("Plate");
                    if (plate.equals("null")) {
                        handheldFragmentWeakReference.get().getActivity()
                                .runOnUiThread(new Runnable() {
                                    public void run() {
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(handheldFragmentWeakReference.get().getActivity());
                                        alertDialogBuilder.setTitle("Vehicle Control");
                                        alertDialogBuilder.setMessage("Vehicle not found!");
                                        alertDialogBuilder.setCancelable(false);
                                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                        alertDialogBuilder.create().show();
                                    }
                                });
                    } else {
                        Intent intent = new Intent(handheldFragmentWeakReference.get().getActivity(),
                                VehicleActivity.class);
                        intent.putExtra(VEHICLE_MESSAGE, plate);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        //MainActivity.this.displayVehicle(plate);
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

}
