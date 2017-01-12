package com.example.tacademy.uitest2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.tacademy.uitest2.Model.ImageProc;
import com.example.tacademy.uitest2.Model.ResponseDaumAPISearchImage;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    //통신 1회당 담는 그릇(매번 변경됨)
    ResponseDaumAPISearchImage results;
    //실제 검색 결과를 담는 그릇(계속해서 누적됨)
    ArrayList<ResponseDaumAPISearchImage.channel.item> items
            = new ArrayList<ResponseDaumAPISearchImage.channel.item>();
    DaumSearchAdapter daumSearchAdapter;
    int pageNo = 1;
    int curPageNo;

    @BindView(R.id.listView)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        ImageProc.getInstance().getImageLoader(this); // 초기화

        daumSearchAdapter = new DaumSearchAdapter();
        listView.setAdapter(daumSearchAdapter);
        listView.setDividerHeight(0);

        getSearchImage();
    }

    // 1페이지 이미지 검색 결과 가져오기
    public void getSearchImage() {
        // 통신 string -> string
        // 1. 통신큐 획득 (전체 앱상에서 1회만 수행)
        RequestQueue queue = Volley.newRequestQueue(this);
        // 2. 요청 준비
        StringRequest sr = new StringRequest(Request.Method.GET,
                "https://apis.daum.net/search/image?apikey=614e79fcc272121933e4875501a83ba4&q=%EC%B9%B4%EC%B9%B4%EC%98%A4%ED%94%84%EB%A0%8C%EC%A6%88&output=json&pageno=" + pageNo,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("UI", response);
                        results = new Gson().fromJson(response, ResponseDaumAPISearchImage.class);
                        items.addAll(results.getChannel().getItem());
                        curPageNo = pageNo;

                        //데이터가 변경되었음을 통보한다 -> 리스트뷰가 반응하여 다시 갱신을 진행
                        daumSearchAdapter.notifyDataSetChanged();
                        for (ResponseDaumAPISearchImage.channel.item item : results.getChannel().getItem()) {
                            Log.i("UI", item.getTitle());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> maps = new HashMap<String, String>();
                maps.put("apikey", "614e79fcc272121933e4875501a83ba4");
                maps.put("q", "%EC%B9%B4%EC%B9%B4%EC%98%A4%ED%94%84%EB%A0%8C%EC%A6%88");
                maps.put("output", "json");
                maps.put("pageno", "1");
                return maps;

                // return super.getParams();
            }
        };
        // 3. 큐에 요청 넣기
        queue.add(sr);

    }

    // ListView를 구성하는 각 셀뷰의 위젯에 대응하는 클래스
    // -> xml에서 자바로 찾는 과정이 반응속도를 떨어뜨리는 원인
    class ViewHolder {
        @BindView(R.id.thumbnail)
        CircleImageView thumbnail;

        @BindViews({R.id.title, R.id.pubDate, R.id.link})
        List<TextView> texts;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    // 아답터
    class DaumSearchAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return items.size();
            // if (results == null) return 0;
           // return results.getChannel().getItem().size();
        }

        @Override
        public ResponseDaumAPISearchImage.channel.item getItem(int position) {
            return items.get(position);
            //results.getChannel().getItem().get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {
                //xml에 있는 것을 view로 만들어내는 과정 inflater
                convertView = getLayoutInflater().inflate(R.layout.cell_listview_layout, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ResponseDaumAPISearchImage.channel.item item = getItem(position);

            // 데이터 세팅!  //계속해서 변경된 내용을 보여줘야함
            // 썸네일
            ImageProc.getInstance().drawImage(item.getThumbnail(), viewHolder.thumbnail);
            // 제목
            viewHolder.texts.get(0).setText(Html.fromHtml(item.getTitle()));
            viewHolder.texts.get(0).setText(Html.fromHtml(viewHolder.texts.get(0).getText().toString()));
            // 공개일
            viewHolder.texts.get(1).setText(Html.fromHtml(item.getPubDate()));
            viewHolder.texts.get(1).setText(Html.fromHtml(viewHolder.texts.get(1).getText().toString()));
            // 링크
            viewHolder.texts.get(2).setText(Html.fromHtml(item.getLink()));
            viewHolder.texts.get(2).setText(Html.fromHtml(viewHolder.texts.get(2).getText().toString()));

            // 마지막 체크
            if (position == getCount() - 1) {
                Toast.makeText(MainActivity.this, "마지막", Toast.LENGTH_SHORT).show();
                Log.i("UI", "마지막");
                if (pageNo == curPageNo) {
                    pageNo++;
                    //통신
                    getSearchImage();
                }
            }

            return convertView;
        }
    }
}
