package uk.ac.bath.dietpi.ui.goals;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import uk.ac.bath.dietpi.DBHandler;
import uk.ac.bath.dietpi.MainActivity;
import uk.ac.bath.dietpi.R;
import uk.ac.bath.dietpi.databinding.FragmentGoalsBinding;

public class GoalsFragment extends Fragment {

    private FragmentGoalsBinding binding;
    private EditText editTextGoal;
    private Button btnChangeGoal;
    private TextView textDisplayGoal;
    private TextView displayProgressTextView;
    private AutoCompleteTextView autoCompleteTextView;
    private TextView displayStreaksTextView;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    public static final String DATE_TEXT = "date";
    public static final String MACRO = "macro";
    public static final String STREAK = "0";
    public static final String CHECK = "false";

    private String savedText;
    private String selectedMacronutrient;
    private String ongoingStreak;
    private String currentlySavedDate;
    private String checked;

    @Override
    public void onResume() {
        super.onResume();
        String[] macronutrients = getResources().getStringArray(R.array.macronutrients);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(requireContext(), R.layout.dropdown_item, macronutrients);
        binding.autoCompleteTextView.setAdapter(arrayAdapter);
        displayStreaks();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        GoalsViewModel goalsViewModel =
                new ViewModelProvider(this).get(GoalsViewModel.class);
        binding = FragmentGoalsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        String[] macronutrients = getResources().getStringArray(R.array.macronutrients);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(requireContext(), R.layout.dropdown_item, macronutrients);
        binding.autoCompleteTextView.setAdapter(arrayAdapter);

        editTextGoal = binding.editTextGoal;
        btnChangeGoal = binding.btnChangeGoal;
        textDisplayGoal = binding.textDisplayGoal;
        displayProgressTextView = binding.displayProgressTextView;
        autoCompleteTextView = binding.autoCompleteTextView;
        displayStreaksTextView = binding.displayStreaksTextView;

        setSelection();
        displayCurrentProgress();
        displayStreaks();

        String something = textDisplayGoal.getText().toString();

        btnChangeGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newGoal = editTextGoal.getText().toString();
                if(!newGoal.equals("") && !autoCompleteTextView.getText().toString().equals(""))
                {
                    saveData();
                    setCurrentGoalText(newGoal);
                    saveData();
                    setStreaks("0");
                    displayCurrentProgress();
                    setCurrentDate();
                    setNotChecked();
                    displayStreaks();
                }
            }
        });

        loadData();
        return root;
    }

    public void displayStreaks()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        ongoingStreak = sharedPreferences.getString(STREAK, "");
        checked = sharedPreferences.getString(CHECK, "");

        if(ongoingStreak.equals(""))
        {
            ongoingStreak = "0";
        }
        Integer streak_value = Integer.parseInt(ongoingStreak);

        if(checked.equals(""))
        {
            checked = "false";
        }

        currentlySavedDate = sharedPreferences.getString(DATE_TEXT, "");
        if(currentlySavedDate.equals(""))
        {
            currentlySavedDate = getDate();
        }

        String currentDate = getDate();

        String new_save_date = currentlySavedDate.substring(0, 2);
        String new_current_date = currentDate.substring(0, 2);


        int savedDay = Integer.parseInt(new_save_date);
        int currentDay = Integer.parseInt(new_current_date);

        if(currentDay == savedDay + 1)
        {
            setNotChecked();
        }

        String goal = binding.textDisplayGoal.getText().toString();
        String progress = binding.displayProgressTextView.getText().toString();
        if(!goal.equals("") && !progress.equals(" ") && checked.equals("false"))
        {
            if(currentDay == savedDay)
            {
                float goal_val = Float.parseFloat(goal.replaceAll("[^0-9.]", ""));
                float progress_val = Float.parseFloat(progress.replaceAll("[^0-9.]", ""));

                if(progress_val >= goal_val)
                {
                    streak_value += 1;
                    setChecked();
                    setStreaks(streak_value.toString());
                }
            }
            else if(currentDay == savedDay + 1)
            {
                float goal_val = Float.parseFloat(goal.replaceAll("[^0-9.]", ""));
                float progress_val = Float.parseFloat(progress.replaceAll("[^0-9.]", ""));

                if(progress_val >= goal_val)
                {
                    streak_value += 1;
                    setChecked();
                    setStreaks(streak_value.toString());
                    setCurrentDate();
                }
            }
            else
            {
                streak_value = 0;
                setStreaks("0");
                setCurrentDate();
            }
        }


        displayStreaksTextView.setText(streak_value.toString());
    }

    public void setChecked()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(CHECK, "true");
        editor.apply();
    }

    public void setNotChecked()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(CHECK, "false");
        editor.apply();
    }

    public String getDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }

    public void setCurrentDate()
    {
        String a_new_date = getDate();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(DATE_TEXT, a_new_date);
        editor.apply();
    }

    public void setStreaks(String streak)
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(STREAK, streak);
        editor.apply();
    }

    public void setSelection()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        selectedMacronutrient = sharedPreferences.getString(MACRO, "");

        if(!selectedMacronutrient.equals(null))
        {
            autoCompleteTextView.setText(selectedMacronutrient);
        }
        else
        {
            autoCompleteTextView.setText("Calories (kcal)");
        }
    }

    public void setCurrentGoalText(String newGoal)
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        selectedMacronutrient = sharedPreferences.getString(MACRO, "");

        if(!newGoal.equals(""))
        {
            textDisplayGoal.setText(newGoal + " " + selectedMacronutrient);
        }
        editTextGoal.setText("");
    }

    public void displayCurrentProgress()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date currentDate = new Date();
        String todays_date = dateFormat.format(currentDate);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        selectedMacronutrient = sharedPreferences.getString(MACRO, "");
        DBHandler dbHandler = ((MainActivity) getActivity()).getDbHandler();

        Hashtable<String,Float> hT = dbHandler.retrieveTotal(todays_date);
        String calorie_count;

        if(selectedMacronutrient.equals("Calories (kcal)"))
        {
            calorie_count = hT.get("Calories").toString();
        }
        else if(selectedMacronutrient.equals("Fat (g)"))
        {
            calorie_count = hT.get("Fat").toString();
        }
        else if(selectedMacronutrient.equals("Protein (g)"))
        {
            calorie_count = hT.get("Protein").toString();
        }
        else if(selectedMacronutrient.equals("Carbs (g)"))
        {
            calorie_count = hT.get("Carbohydrates").toString();
        }
        else
        {
            calorie_count = "";
        }

        displayProgressTextView.setText(calorie_count + " " + selectedMacronutrient);

    }

    public void saveData()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(TEXT, textDisplayGoal.getText().toString());
        editor.putString(MACRO, autoCompleteTextView.getText().toString());

        editor.apply();
    }

    public void loadData()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        savedText = sharedPreferences.getString(TEXT, "");
        if(savedText.equals("Calories (kcal)"))
        {
            textDisplayGoal.setText("");
        }
        else
        {
            textDisplayGoal.setText(savedText);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        selectedMacronutrient = sharedPreferences.getString(MACRO, "");

        String currentNutrient = autoCompleteTextView.getText().toString();
        if(currentNutrient.equals(selectedMacronutrient))
        {
            saveData();
        }
        binding = null;
    }
}