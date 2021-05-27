package com.calculator.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private ListView historyList;
    private Button histortViewbut;
    private List<String> arrayList;
    private FirebaseFirestore db;
   private ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arrayList = new ArrayList<>();
        getLastTenCalList();

        historyList = findViewById(R.id.historyList);
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
//        historyList.setAdapter(arrayAdapter);

        EditText edittext = (EditText)findViewById(R.id.input);
        TextView results = findViewById(R.id.results);

        histortViewbut = findViewById(R.id.historyButton);
        histortViewbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyList.setVisibility(View.VISIBLE);
            }
        });

        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    results.setText("");
                    String str = edittext.getText().toString().trim();
                    results.setText(str);
                    String regex = "[0-9]+";
                    if( str.contains("..")||str.contains("++")||str.contains("--")||str.contains("**")||
                            str.contains("//")||str.contains("+-")||str.contains("*-")||str.contains("/-")||str.contains("/+")||str.contains("/*")||str.contains("+++"))
                    {
                        Toast.makeText(getApplicationContext(),"Invalid Expression",Toast.LENGTH_LONG).show();
                    } else {
                        int equation = mads(str);
                        String equationString = String.valueOf(equation);
                        String pastResults = str + ""+"="+""+""+ equationString;
                        if(!str.matches(regex)){
                            arrayList.add(pastResults);
                        }
                        updateLastTenCalFireStore();
                        if(arrayList.size() > 10){
                            arrayList = arrayList.subList(arrayList.size()- 10, arrayList.size());
                            updateLastTenCalFireStore();
                            arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
                            historyList.setAdapter(arrayAdapter);
                            arrayAdapter.notifyDataSetChanged();

                        }

                        arrayAdapter.notifyDataSetChanged();
                        results.setText(String.valueOf(equation));
                        edittext.setText(equationString);
                    }
                    handled = true;
                }
                return handled;
            }
        });



    }

    private void updateLastTenCalFireStore() {
        DocumentReference docRef = db.collection("data").document("one");
        docRef.update("pastCalculations", arrayList);
    }

    private void getLastTenCalList() {
     db =  FirebaseFirestore.getInstance();
        db.collection("data").document("one").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.get("pastCalculations") != null ) {

                        arrayList = (List<String>)document.get("pastCalculations");
                        Toast.makeText(MainActivity.this, arrayList.toString(), Toast.LENGTH_SHORT).show();
                        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
                        historyList.setAdapter(arrayAdapter);

                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("MainActivity", "get failed with ", task.getException());
                }
            }
        });
    }

    public static int mads(String expression)
    {
        char[] tokens = expression.toCharArray();


        Stack<Integer> values = new
                Stack<Integer>();


        Stack<Character> ops = new
                Stack<Character>();

        for (int i = 0; i < tokens.length; i++)
        {
            if (tokens[i] == ' ')
                continue;


            if (tokens[i] >= '0' &&
                    tokens[i] <= '9')
            {
                StringBuffer sbuf = new
                        StringBuffer();

                while (i < tokens.length &&
                        tokens[i] >= '0' &&
                        tokens[i] <= '9')
                    sbuf.append(tokens[i++]);
                values.push(Integer.parseInt(sbuf.
                        toString()));
                i--;
            }



            else if (tokens[i] == '+' ||
                    tokens[i] == '-' ||
                    tokens[i] == '*' ||
                    tokens[i] == '/')
            {

                while (!ops.empty() &&
                        hasPrecedence(tokens[i],
                                ops.peek()))
                    values.push(applyOp(ops.pop(),
                            values.pop(),
                            values.pop()));

                ops.push(tokens[i]);
            }
        }

        while (!ops.empty())
            values.push(applyOp(ops.pop(),
                    values.pop(),
                    values.pop()));


        return values.pop();
    }

    public static boolean hasPrecedence(
            char op1, char op2)
    {

        if ((op1 == '*' || op1 == '+') &&
                (op2 == '/' || op2 == '-'))
            return false;
        else
            return true;
    }

    public static int applyOp(char op,
                              int b, int a)
    {
        switch (op)
        {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new
                            UnsupportedOperationException(
                            "Cannot divide by zero");
                return a / b;
        }
        return 0;
    }

}