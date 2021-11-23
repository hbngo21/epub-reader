package com.folioreader.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.DrawActivity;
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

    @Override
    public void editNote(final HighlightImpl highlightImpl, final int position) {
        this.curHighlightImpl = highlightImpl;
        this.curPosition = position;
        final AlertDialog.Builder choices = new AlertDialog.Builder(getActivity());
//        String text = highlightImpl.getNote();
//        String[] choiceArr = {};
//        TextUtils.isEmpty(text) ? new String[] {"Text", "Draw", "Webview", "Clear note"};
//        if (text.length() == 0) {
//            choiceArr = new String[] {"Text", "Draw", "Webview", "Clear note"};
//        }
//        else if (text.length() <= 5) {
//            choiceArr = new String[] {"Text", "Draw", "Webview", "Clear note"};
//        }
        choices.setTitle("Pick a note type")
                .setItems(new String[] {"Text", "Draw", "Webview", "Clear note"}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dial, int which) {
                // Choose text note
                if (which == 0) {
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
                // Choose draw note
                else if (which == 1) {
                    Intent intent = new Intent(getActivity(), DrawActivity.class);
//                    startActivity(intent);
                    startActivityForResult(intent, 100);
                }
                // Choose web view
                else if (which == 2) {

                }
                // Clear note
                else if (which == 3) {
                    String note = "";
                    highlightImpl.setNote(note);
                    if (HighLightTable.updateHighlight(highlightImpl)) {
                        HighlightUtil.sendHighlightBroadcastEvent(
                                HighlightFragment.this.getActivity().getApplicationContext(),
                                highlightImpl,
                                HighLight.HighLightAction.MODIFY);
                        adapter.editNote(note, position);
                    }
                }
            }
        });
        choices.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("After intent", "Successfulllllllllllllllllllllllllllllllllllll");
        if (requestCode == 100) {
            if (resultCode == 100) {
                String note = "<img>" + data.getStringExtra("bitmap");
//                Toast.makeText(getActivity(), note, Toast.LENGTH_SHORT).show();
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
    }
}


