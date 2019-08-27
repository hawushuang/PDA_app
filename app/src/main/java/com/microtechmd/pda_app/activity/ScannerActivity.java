package com.microtechmd.pda_app.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.zxing.Result;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.ISBNParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ProductParsedResult;
import com.google.zxing.client.result.TextParsedResult;
import com.google.zxing.client.result.URIParsedResult;
import com.microtechmd.pda_app.ActivityPDA;
import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.manager.BroadcastReceiveManager;
import com.microtechmd.pda_app.util.MediaUtil;
import com.microtechmd.pda_app.widget.StateButton;
import com.mylhyl.zxing.scanner.OnScannerCompletionListener;
import com.mylhyl.zxing.scanner.ScannerView;
import com.mylhyl.zxing.scanner.common.Scanner;
import com.mylhyl.zxing.scanner.result.AddressBookResult;
import com.mylhyl.zxing.scanner.result.ISBNResult;
import com.mylhyl.zxing.scanner.result.ProductResult;
import com.mylhyl.zxing.scanner.result.URIResult;

import java.io.IOException;


public class ScannerActivity extends ActivityPDA {
    public static final int REQUEST_CODE_SCANNER = 100;

    private ImageButton back;
    private ScannerView mScannerView;
    private ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        back = findViewById(R.id.ibt_back);
        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setChecked(true);
        mScannerView = findViewById(R.id.scanner_view);

//        mScannerView.setMediaResId(R.raw.weixin_beep);//设置扫描成功的声音
        initClick();
    }

    @Override
    protected void onResume() {
        mScannerView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mScannerView.onPause();
        super.onPause();
    }

    private void initClick() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mScannerView.toggleLight(!isChecked);
            }
        });
        mScannerView.setOnScannerCompletionListener(new OnScannerCompletionListener() {
            @Override
            public void onScannerCompletion(Result rawResult, ParsedResult parsedResult, Bitmap barcode) {
                try {
                    MediaPlayer mMediaPlayer = new MediaPlayer();
                    AssetFileDescriptor fileDescriptor = ScannerActivity.this.getAssets().openFd("weixin_beep.ogg");
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                            fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                    mMediaPlayer.setVolume(0.2f, 0.2f);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (rawResult == null) {
                    showToast("未发现二维码");
                    finish();
                    return;
                }
                final Bundle bundle = new Bundle();
                final ParsedResultType type = parsedResult.getType();
                switch (type) {
                    case ADDRESSBOOK:
                        AddressBookParsedResult addressBook = (AddressBookParsedResult) parsedResult;
                        bundle.putSerializable(Scanner.Scan.RESULT, new AddressBookResult(addressBook));
                        break;
                    case PRODUCT:
                        ProductParsedResult product = (ProductParsedResult) parsedResult;
                        bundle.putSerializable(Scanner.Scan.RESULT, new ProductResult(product));
                        break;
                    case ISBN:
                        ISBNParsedResult isbn = (ISBNParsedResult) parsedResult;
                        bundle.putSerializable(Scanner.Scan.RESULT, new ISBNResult(isbn));
                        break;
                    case URI:
                        URIParsedResult uri = (URIParsedResult) parsedResult;
                        bundle.putSerializable(Scanner.Scan.RESULT, new URIResult(uri));
                        startActivity(new Intent(ScannerActivity.this, UriActivity.class).putExtras(bundle));
                        finish();
                        break;
                    case TEXT:
                        Intent intent = getIntent();
                        intent.putExtra(Scanner.Scan.RESULT, rawResult.getText());
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        return;
                    case GEO:
                        break;
                    case TEL:
                        break;
                    case SMS:
                        break;
                    default:
                        break;
                }
            }
        });
    }
}
