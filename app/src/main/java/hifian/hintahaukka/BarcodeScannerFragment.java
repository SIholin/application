package hifian.hintahaukka;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;
import static java.lang.Boolean.FALSE;


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

        //selectedStore = getIntent().getExtras().getString("selectedStore");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
//                Toast.makeText(getContext(), "Permission is granted!", Toast.LENGTH_LONG).show();
            } else {
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
                if(scannerView == null) {
                    //scannerView = new ZXingScannerView(getContext());
                    //setContentView(scannerView);
                }
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
        onDestroy();
        final String scanResult = result.getText();
        if (checkEan13(scanResult)) {
             Navigation.findNavController(getView()).navigate(
                BarcodeScannerFragmentDirections.actionBarcodeScannerFragmentToEnterPriceFragment(selectedStore, scanResult, test));

        } else {
            // Barcode was not correct EAN13
            Toast.makeText(getContext(), "Viivakoodi ei ole oikein! ", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkEan13(String scanResult) {
        if (scanResult.length()!=13) return FALSE;
        int odd=0;
        int even=0;
        int num;
        for (int i=0;i<12;i++) {
            num = Integer.parseInt(scanResult.substring(i, i+1));
            if (i%2==0) {
                odd += num;
            } else {
                even += num;
            }
        }
        int sum = even*3 + odd;
        int nextTen = (sum/10 + 1)*10;
        return  (nextTen-sum)==Integer.parseInt(scanResult.substring(12,13));

    }
}
