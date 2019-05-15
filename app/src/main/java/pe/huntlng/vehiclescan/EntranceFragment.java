package pe.huntlng.vehiclescan;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class EntranceFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{


    private OnEntranceFragmentInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    public EntranceFragment() {
        // Required empty public constructor
    }


    public static EntranceFragment newInstance(String param1, String param2) {
        EntranceFragment fragment = new EntranceFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onRefresh() {
        updateEntrances();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entrance,
                container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.list_Entrance_Layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        updateEntrances();
        return view;
    }

    private void updateEntrances() {

        String serverURL = getResources().getString(R.string.service_url)
                + "/VehicleLogService/Log/1";
        Log.d("URL Entrance", serverURL);
        EntranceOperationTask entranceOperationTask = new EntranceOperationTask(
                this);

        entranceOperationTask.execute(serverURL);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEntranceFragmentInteractionListener) {
            mListener = (OnEntranceFragmentInteractionListener) context;
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


    public interface OnEntranceFragmentInteractionListener {
        // TODO: Update argument type and name
        void onEntranceFragmentInteraction();
    }

    private static class EntranceOperationTask extends
            AsyncTask<String, String, String> {
        HttpURLConnection urlConnection;
        ArrayList<HashMap<String, String>> list;
        ListView entranceList = null;
        private WeakReference<EntranceFragment> entranceFragmentWeakReference;

        private EntranceOperationTask(EntranceFragment fragment) {
            this.entranceFragmentWeakReference = new WeakReference<EntranceFragment>(
                    fragment);

        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();

        }

        protected void onPostExecute(String result) {
            try {
                entranceList = (ListView) entranceFragmentWeakReference
                        .get().getView().findViewById(R.id.list_Entrance);
            } catch (NullPointerException nullPointerException) {
                return;
            }
            try {
                if (!result.equals("")) {
                    //JSONObject jsonResponse = new JSONObject(result);
                    JSONArray jsonArray = new JSONArray(result);

                    list = new ArrayList<HashMap<String, String>>();
                    for (int i = 0; i < jsonArray.length(); i++) {

                        HashMap<String, String> item = new HashMap<String, String>();
                        item.put("Plate", jsonArray.getJSONObject(i)
                                .optString("Plate"));
                        item.put(
                                "Type",
                                jsonArray.getJSONObject(i).optString(
                                        "Type"));
                        item.put(
                                "Contractor",
                                jsonArray.getJSONObject(i).optString(
                                        "Contractor"));
                        item.put(
                                "Time",
                                jsonArray.getJSONObject(i).optString(
                                        "LogDate"));
                        list.add(item);
                    }
                    entranceFragmentWeakReference.get().getActivity()
                            .runOnUiThread(new Runnable() {
                                public void run() {
                                    String[] columns = new String[] {
                                            "Plate", "Type", "Contractor", "Time" };
                                    int[] renderTo = new int[] {
                                            R.id.plate, R.id.type,
                                            R.id.contractor, R.id.time };
                                    ListAdapter listAdapter = new SimpleAdapter(
                                            entranceFragmentWeakReference.get()
                                                    .getActivity(), list,
                                            R.layout.journallog_list_row,
                                            columns, renderTo);
                                    entranceList.setAdapter(listAdapter);

                                }
                            });
                }
            } catch (Exception e) {

            }
        }


    }
}
