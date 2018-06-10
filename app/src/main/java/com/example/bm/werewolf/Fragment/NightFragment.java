package com.example.bm.werewolf.Fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bm.werewolf.Activity.PlayActivity;
import com.example.bm.werewolf.Adapter.DayAdapter;
import com.example.bm.werewolf.Adapter.RoleListViewAdapter;
import com.example.bm.werewolf.Model.PlayerModel;
import com.example.bm.werewolf.R;
import com.example.bm.werewolf.Utils.Constant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * A simple {@link Fragment} subclass.
 */
public class NightFragment extends Fragment {
    private static final String TAG = "NightFragment";

    @BindView(R.id.gv_player)
    GridViewWithHeaderAndFooter gvPlayer;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.tv_skip)
    TextView tvSkip;
    @BindView(R.id.tv_saving)
    TextView tvSaving;
    @BindView(R.id.tv_not_saving)
    TextView tvNotSaving;
    @BindView(R.id.tv_timer)
    TextView tvTimer;
    Unbinder unbinder;

    Context context;
    @BindView(R.id.iv_roles)
    ImageView ivRoles;
    @BindView(R.id.lv_roles)
    ListView lvRoles;

    View view;

    public NightFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_night, container, false);
        unbinder = ButterKnife.bind(this, view);
        context = getContext();

        RoleListViewAdapter roleListViewAdapter = new RoleListViewAdapter(RoleReceiveFragment.roleList);
        lvRoles.setAdapter(roleListViewAdapter);

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View header = layoutInflater.inflate(R.layout.layout_header, null);
        gvPlayer.addHeaderView(header);

        ImageView ivRole = header.findViewById(R.id.iv_role);
        TextView tvRole = header.findViewById(R.id.tv_role);

        ivRole.setImageResource(Constant.imageRole[PlayActivity.currentRole + 1]);
        tvRole.setText(Constant.nameRole[PlayActivity.currentRole + 1]);

        if (PlayActivity.currentRole == Constant.PHU_THUY) witchInit();
        else normalInit();

        new CountDownTimer(15000 + PlayActivity.startTime - System.currentTimeMillis(), 1000) {

            public void onTick(long millisUntilFinished) {
                if (tvTimer == null) tvTimer = view.findViewById(R.id.tv_timer);
                tvTimer.setText("" + (millisUntilFinished / 1000));
            }

            public void onFinish() {
                if (tvConfirm == null) tvConfirm = view.findViewById(R.id.tv_confirm);
                if (tvSkip == null) tvSkip = view.findViewById(R.id.tv_skip);
                if (tvSaving == null) tvSaving = view.findViewById(R.id.tv_saving);
                if (tvNotSaving == null) tvNotSaving = view.findViewById(R.id.tv_not_saving);
                tvConfirm.setVisibility(View.GONE);
                tvSkip.setVisibility(View.GONE);
                tvSaving.setVisibility(View.GONE);
                tvNotSaving.setVisibility(View.GONE);
                if (tvTimer == null) tvTimer = view.findViewById(R.id.tv_timer);
                tvTimer.setText("0");
            }

        }.start();

        return view;
    }

    void normalInit() {
        tvConfirm.setVisibility(View.VISIBLE);
        tvSkip.setVisibility(View.VISIBLE);
        tvSaving.setVisibility(View.GONE);
        tvNotSaving.setVisibility(View.GONE);
        if (PlayActivity.currentRole == Constant.PHU_THUY && !PlayActivity.toxicPotion)
            tvConfirm.setVisibility(View.GONE);

        List<PlayerModel> playerModelList = new ArrayList<>();
        for (PlayerModel playerModel : Constant.listPlayerModel)
            if (playerModel.alive)
                if (playerModel.role != PlayActivity.currentRole || PlayActivity.currentRole == Constant.BAO_VE)
                    if ((PlayActivity.currentRole != Constant.BAO_VE || !playerModel.id.equals(PlayActivity.lastProtectedPlayerID))
                            && (PlayActivity.currentRole != Constant.THO_SAN || !playerModel.id.equals(PlayActivity.lastTargetPlayerID)))
                        playerModelList.add(playerModel);

        DayAdapter dayAdapter = new DayAdapter(playerModelList);
        gvPlayer.setAdapter(dayAdapter);
    }

    void witchInit() {
        tvConfirm.setVisibility(View.GONE);
        tvSkip.setVisibility(View.GONE);
        tvSaving.setVisibility(View.VISIBLE);
        tvNotSaving.setVisibility(View.VISIBLE);
        if (!PlayActivity.healPotion) tvSaving.setVisibility(View.GONE);

        FirebaseDatabase.getInstance().getReference("Ingame Data").child(Constant.roomID)
                .child("dyingPlayer").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<PlayerModel> playerModelList = new ArrayList<>();

                if (dataSnapshot.getValue() != null)
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        if (!snapshot.getValue(String.class).equals(""))
                            for (PlayerModel playerModel : Constant.listPlayerModel)
                                if (playerModel.id.equals(snapshot.getValue(String.class)))
                                    playerModelList.add(playerModel);

                if (gvPlayer == null) gvPlayer = view.findViewById(R.id.gv_player);
                DayAdapter dayAdapter = new DayAdapter(playerModelList);
                gvPlayer.setAdapter(dayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.iv_roles, R.id.tv_confirm, R.id.tv_skip, R.id.tv_saving, R.id.tv_not_saving})
    public void onViewClicked(View view) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Ingame Data").child(Constant.roomID)
                .child("response");

        switch (view.getId()) {
            case R.id.iv_roles:
                if (lvRoles.getVisibility() == View.VISIBLE)
                    lvRoles.setVisibility(View.GONE);
                else
                    lvRoles.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_confirm:
                if (DayAdapter.pick == -1) {
                    Toast.makeText(getContext(), "Cần chọn mục tiêu trước!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tvConfirm == null) tvConfirm = view.findViewById(R.id.tv_confirm);
                if (tvSkip == null) tvSkip = view.findViewById(R.id.tv_skip);
                tvConfirm.setVisibility(View.GONE);
                tvSkip.setVisibility(View.GONE);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> responseList = new ArrayList<>();
                        if (dataSnapshot.getValue() != null)
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                responseList.add(snapshot.getValue(String.class));
                        responseList.add(DayAdapter.playerModelList.get(DayAdapter.pick).id);
                        databaseReference.setValue(responseList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if (PlayActivity.currentRole == Constant.BAO_VE)
                    PlayActivity.lastProtectedPlayerID = DayAdapter.playerModelList.get(DayAdapter.pick).id;
                if (PlayActivity.currentRole == Constant.THO_SAN)
                    PlayActivity.lastTargetPlayerID = DayAdapter.playerModelList.get(DayAdapter.pick).id;
                break;
            case R.id.tv_skip:
                if (tvConfirm == null) tvConfirm = view.findViewById(R.id.tv_confirm);
                if (tvSkip == null) tvSkip = view.findViewById(R.id.tv_skip);
                tvConfirm.setVisibility(View.GONE);
                tvSkip.setVisibility(View.GONE);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> responseList = new ArrayList<>();
                        if (dataSnapshot.getValue() != null)
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                responseList.add(snapshot.getValue(String.class));
                        responseList.add("");
                        databaseReference.setValue(responseList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if (PlayActivity.currentRole == Constant.BAO_VE)
                    PlayActivity.lastProtectedPlayerID = "";
                if (PlayActivity.currentRole == Constant.THO_SAN)
                    PlayActivity.lastTargetPlayerID = "";
                break;
            case R.id.tv_not_saving:
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> responseList = new ArrayList<>();
                        if (dataSnapshot.getValue() != null)
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                responseList.add(snapshot.getValue(String.class));
                        responseList.add("");
                        databaseReference.setValue(responseList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                normalInit();
                break;
            case R.id.tv_saving:
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<String> responseList = new ArrayList<>();
                        if (dataSnapshot.getValue() != null)
                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                                responseList.add(snapshot.getValue(String.class));
                        responseList.add("saving");
                        databaseReference.setValue(responseList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                normalInit();
                break;
        }
    }
}
