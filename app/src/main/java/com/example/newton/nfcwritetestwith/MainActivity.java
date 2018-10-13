package com.example.newton.nfcwritetestwith;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        detectOperation(i);


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);


    }

    @Override
    public void onNewIntent(Intent intent) {
        detectOperation(intent);
    }

    public void detectOperation(Intent intent) {
        Tag detectedTag;
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Ndef ndef = Ndef.get(detectedTag);
            try {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                NdefRecord[] ndefRecord = ndefMessage .getRecords();
//                Log.e(TAG, "ndefRecord content:  " + toHexString(ndefRecord,0,ndefRecord.length,true));
            } catch (FormatException | IOException e) {
                e.printStackTrace();
            }
//            writeNfcTag
            String tagText = readNfcTag(intent);
            Toast.makeText(this, tagText, Toast.LENGTH_SHORT).show();
        }
    }
    private String readNfcTag(Intent intent) {
        String mTagText = "";
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msgs[] = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            try {
                if (msgs != null) {
                    NdefRecord record = msgs[0].getRecords()[0];
                    mTagText = parseTextRecord(record);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mTagText;
    }
    public static String parseTextRecord(NdefRecord ndefRecord) {
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0x3f;
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
//        Numeric.hexStringToByteArray()
    }

}
