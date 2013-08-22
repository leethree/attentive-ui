package hk.hku.cs.srli.factfinder.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import hk.hku.cs.srli.factfinder.FFApp;
import hk.hku.cs.srli.factfinder.DataSet.DataItem;
import hk.hku.cs.srli.factfinder.R;

public class FFDialog extends DialogFragment {

    private DataItem mFact;
    private NumberPicker np;
    private DialogInterface.OnClickListener mListener;
    
    public static FFDialog newInstance(int section, int id) {
        FFDialog f = new FFDialog();

        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putInt("section", section);
        f.setArguments(args);

        return f;
    }
    
    public void setListener(DialogInterface.OnClickListener mListener) {
        this.mListener = mListener;
    }
    
    public int getNumber() {
        return np.getValue();
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mFact = FFApp.getData(getActivity()).getItem(
                getArguments().getInt("section"), getArguments().getInt("id"));
        
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog, null);
        np = (NumberPicker) view.findViewById(R.id.numberPicker1);
        np.setMinValue(1);
        np.setMaxValue(10);
        // workaround to disable soft keyboard for NumberPicker
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mFact.name).setView(view)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FFApp.log("Detail UI", "Cancel 'add multiple' dialog.");
                        FFDialog.this.dismiss();
                    }
                });
        if (mListener != null)
            builder.setPositiveButton("OK", mListener);

        return builder.create();
    }
    
    @Override
    public void onDismiss(DialogInterface dialog) {
        FFApp.log("Detail UI", "Dismiss 'add multiple' dialog.");
        super.onDismiss(dialog);
    }
    
}
