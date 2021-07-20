package com.example.ximalaya.views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ximalaya.R;

public class ConfirmCheckBoxDialog extends Dialog {

    private TextView mCancelBtn;
    private TextView mConfirm;
    private OnDialogActionClickListener mClickListener = null;
    private CheckBox mCheckBox;

    public ConfirmCheckBoxDialog(@NonNull Context context) {
        this(context,0);
    }

    public ConfirmCheckBoxDialog(@NonNull Context context, int themeResId) {
        this(context, true,null);
    }

    protected ConfirmCheckBoxDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        setContentView(R.layout.dialog_check_box_confirm);
        initView();
        initListener();
    }

    private void initListener() {
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    boolean checked = mCheckBox.isChecked();
                    mClickListener.onConfirmClick(checked);
                    dismiss();
                }
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {

                    mClickListener.onCancelSubClick();
                    dismiss();
                }
            }
        });
    }

    private void initView(){
        mCancelBtn = this.findViewById(R.id.dialog_check_box_cancel);
        mConfirm = this.findViewById(R.id.dialog_check_box_confirm);
        mCheckBox = this.findViewById(R.id.dialog_check_box);
    }

    public void setOnDialogActionClickListener(OnDialogActionClickListener listener){
        this.mClickListener = listener;
    }

    public interface OnDialogActionClickListener{

        void onCancelSubClick();
        void onConfirmClick(boolean checked);
    }
}
