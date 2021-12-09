package com.folioreader.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.ui.activity.DrawActivity;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.event.UpdateHighlightEvent;
import com.folioreader.model.sqlite.HighLightTable;
import com.folioreader.ui.adapter.HighlightAdapter;
import com.folioreader.util.AppUtil;
import com.folioreader.util.HighlightUtil;
import org.greenrobot.eventbus.EventBus;

import com.folioreader.ui.activity.MiniBrowserActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class HighlightFragment extends Fragment implements HighlightAdapter.HighLightAdapterCallback {
    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private View mRootView;
    private HighlightAdapter adapter;
    private String mBookId;
    private HighlightImpl curHighlightImpl;
    private int curPosition;


    public static HighlightFragment newInstance(String bookId, String epubTitle) {
        HighlightFragment highlightFragment = new HighlightFragment();
        Bundle args = new Bundle();
        args.putString(FolioReader.EXTRA_BOOK_ID, bookId);
        args.putString(Constants.BOOK_TITLE, epubTitle);
        highlightFragment.setArguments(args);
        return highlightFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_highlight_list, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView highlightsView = (RecyclerView) mRootView.findViewById(R.id.rv_highlights);
        Config config = AppUtil.getSavedConfig(getActivity());
        mBookId = getArguments().getString(FolioReader.EXTRA_BOOK_ID);

        if (config.isNightMode()) {
            mRootView.findViewById(R.id.rv_highlights).
                    setBackgroundColor(ContextCompat.getColor(getActivity(),
                            R.color.black));
        }
        highlightsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        highlightsView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        adapter = new HighlightAdapter(getActivity(), HighLightTable.getAllHighlights(mBookId), this, config);
        highlightsView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(HighlightImpl highlightImpl) {
//        Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
        Log.i("Clicked", "clicked");
        Intent intent = new Intent();
        intent.putExtra(HIGHLIGHT_ITEM, highlightImpl);
        intent.putExtra(Constants.TYPE, Constants.HIGHLIGHT_SELECTED);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void deleteHighlight(int id) {
        if (HighLightTable.deleteHighlight(id)) {
            EventBus.getDefault().post(new UpdateHighlightEvent());
        }
    }

    private void editNoteText(final HighlightImpl highlightImpl, final int position) {
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogCustomTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_notes);
        dialog.show();
        String noteText = highlightImpl.getNote();
        ((EditText) dialog.findViewById(R.id.edit_note)).setText(noteText);

        dialog.findViewById(R.id.btn_save_note).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note =
                        ((EditText) dialog.findViewById(R.id.edit_note)).getText().toString();
                if (!TextUtils.isEmpty(note)) {
                    highlightImpl.setNote(note);
                    if (HighLightTable.updateHighlight(highlightImpl)) {
                        HighlightUtil.sendHighlightBroadcastEvent(
                                HighlightFragment.this.getActivity().getApplicationContext(),
                                highlightImpl,
                                HighLight.HighLightAction.MODIFY);
                        adapter.editNote(note, position);
                    }
                }
                dialog.dismiss();
            }
        });
    }

    private void editNoteDraw(final HighlightImpl highlightImpl, final int position) {
        Intent intent = new Intent(getActivity(), DrawActivity.class);
        String noteText = highlightImpl.getNote();
        if (noteText != null) {
            if (noteText.length() > 5) {
                if (noteText.substring(0, 5).compareTo("<img>") == 0) {
                    Bitmap bit = StringToBitMap(noteText.substring(5));

                    String mPath = getActivity().getApplicationContext().getExternalFilesDir(null) + "/epubviewer/draw.jpg";
                    File imageFile = new File(mPath);
                    if (!imageFile.exists())
                        imageFile.getParentFile().mkdir();
                    try {
                        FileOutputStream outputStream = new FileOutputStream(imageFile);
                        int quality = 100;
                        bit.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                        outputStream.flush();
                        outputStream.close();

                        intent.putExtra("bitmap", mPath);
                    } catch (Throwable e) {
                        // Several error may come out with file handling or DOM
                        e.printStackTrace();
                    }
                }
            }
        }
        startActivityForResult(intent, 100);
    }

    private void editNoteWeb(final HighlightImpl highlightImpl, final int position) {
        Intent intent = new Intent(getActivity(), MiniBrowserActivity.class);
        String noteText = highlightImpl.getContent();
        intent.putExtra("word", noteText);
        startActivityForResult(intent, 200);
    }

    private void editNoteClear(final HighlightImpl highlightImpl, final int position) {
        String note = null;
        highlightImpl.setNote(note);
        if (HighLightTable.updateHighlight(highlightImpl)) {
            HighlightUtil.sendHighlightBroadcastEvent(
                    HighlightFragment.this.getActivity().getApplicationContext(),
                    highlightImpl,
                    HighLight.HighLightAction.MODIFY);
            adapter.editNote(note, position);
        }
    }

    @Override
    public void editNote(final HighlightImpl highlightImpl, final int position) {
        this.curHighlightImpl = highlightImpl;
        this.curPosition = position;
        final AlertDialog.Builder choices = new AlertDialog.Builder(getActivity());

        String temp = highlightImpl.getNote();
        int c = 0;
        if (temp != null) {
            if (!TextUtils.isEmpty(temp)) {
                c = 1;
                if (temp.length() > 5) {
                    if (temp.substring(0, 5).compareTo("<img>") == 0) {
                        c = 2;
                    }
                }
            }
        }
        // Empty note
        if (c == 0)
            choices.setTitle("Pick a note type")
                    .setItems(new String[] {"Text", "Draw", "Webview"}, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dial, int which) {
                    // Choose text note
                    if (which == 0) {
                        editNoteText(highlightImpl, position);
                    }
                    // Choose draw note
                    else if (which == 1) {
                        editNoteDraw(highlightImpl, position);
                    }
                    // Choose web view
                    else if (which == 2) {
                        editNoteWeb(highlightImpl, position);
                    }
                }
            });
        // Text note
        else if (c == 1)
            choices.setTitle("Pick a note type")
                    .setItems(new String[] {"Text", "Clear note"}, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dial, int which) {
                            // Choose text note
                            if (which == 0) {
                                editNoteText(highlightImpl, position);
                            }
                            // Clear note
                            else if (which == 1) {
                                editNoteClear(highlightImpl, position);
                            }
                        }
                    });
        // Draw note
        else
            choices.setTitle("Pick a note type")
                    .setItems(new String[] {"Draw", "Clear note"}, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dial, int which) {
                            // Choose draw note
                            if (which == 0) {
                                editNoteDraw(highlightImpl, position);
                            }
                            // Clear note
                            else if (which == 1) {
                                editNoteClear(highlightImpl, position);
                            }
                        }
                    });
        choices.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == 100) {
                String note = data.getStringExtra("bitmap");
                Bitmap bit = BitmapFactory.decodeFile(note);
                note = "<img>" + BitMapToString(bit);
                if (!TextUtils.isEmpty(note)) {
                    curHighlightImpl.setNote(note);
                    if (HighLightTable.updateHighlight(curHighlightImpl)) {
                        HighlightUtil.sendHighlightBroadcastEvent(
                                HighlightFragment.this.getActivity().getApplicationContext(),
                                curHighlightImpl,
                                HighLight.HighLightAction.MODIFY);
                        adapter.editNote(note, curPosition);
                    }
                }
            }
        }
        else if (requestCode == 200){
            if (resultCode == 200) {
                String img = data.getStringExtra("bitmap");
                Intent intent = new Intent(getActivity(), DrawActivity.class);
                intent.putExtra("bitmap", img);
                startActivityForResult(intent, 100);
            }
        }
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}


