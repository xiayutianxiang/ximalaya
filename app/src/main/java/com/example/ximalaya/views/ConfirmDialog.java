package com.example.ximalaya.views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ximalaya.R;

public class ConfirmDialog extends Dialog {

    private TextView mCancelBtn;
    private TextView mGiveUp;
    private OnDialogActionClickListener mClickListener = null;

    public ConfirmDialog(@NonNull Context context) {
        this(context,0);
    }

    public ConfirmDialog(@NonNull Context context, int themeResId) {
        this(context, true,null);
    }

    protected ConfirmDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        setContentView(R.layout.dialog_confirm);
        initView();
        initListener();
    }

    private void initListener() {
        mGiveUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onGiveUpSubClick();
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
        mCancelBtn = this.findViewById(R.id.dialog_cancel_sub);
        mGiveUp = this.findViewById(R.id.dialog_give_up);
    }

    public void setOnDialogActionClickListener(OnDialogActionClickListener listener){
        this.mClickListener = listener;
    }

    public interface OnDialogActionClickListener{

        void onCancelSubClick();
        void onGiveUpSubClick();
    }
}
