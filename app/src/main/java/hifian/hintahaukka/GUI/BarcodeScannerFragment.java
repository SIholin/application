package hifian.hintahaukka.GUI;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;


import hifian.hintahaukka.R;
import hifian.hintahaukka.Service.BarCodeChecker;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;



public class BarcodeScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {

    public BarcodeScannerFragment() {
        // Required empty public constructor
    }

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private String selectedStore;
    private boolean test;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarcodeScannerFragmentArgs args = BarcodeScannerFragmentArgs.fromBundle(getArguments());
        selectedStore = args.getSelectedStore();
        test = args.getTest();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            }
        }
    }

    private Boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int grantResults[]) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getContext(), "Permission granted!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Permission denied!", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA))
                                displayAlertMessage("You need too allow access for both permissions", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                        }
                                    }
                                });
                            return;
                        }
                    }
                }
                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        scannerView = new ZXingScannerView(getContext());

        // Inflate the layout for this fragment
        return scannerView;
    }

    @Override
    public void handleResult(Result result) {
        final String scanResult = result.getText();
        // Check if barcode is correct EAN13
        BarCodeChecker bcCherker = new BarCodeChecker();
        if (bcCherker.checkEan13(scanResult)) {
            // Ok, move to next fragment
            onDestroy();
             Navigation.findNavController(getView()).navigate(
                BarcodeScannerFragmentDirections.actionBarcodeScannerFragmentToEnterPriceFragment(selectedStore, scanResult, test));
        } else {
            // Not correct, show error message
            Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.text_faulty_barcode, Snackbar.LENGTH_LONG).show();
            onResume();
        }
    }
}

