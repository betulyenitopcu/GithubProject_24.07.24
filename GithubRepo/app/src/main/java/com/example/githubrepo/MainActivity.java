package com.example.githubrepo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput;
    private Button searchButton;
    private TextView userName;
    private ImageView avatar;
    private RecyclerView repoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameInput = findViewById(R.id.usernameInput);
        searchButton = findViewById(R.id.searchButton);
        userName = findViewById(R.id.userName);
        avatar = findViewById(R.id.avatar);
        repoList = findViewById(R.id.repoList);
        repoList.setLayoutManager(new LinearLayoutManager(this));

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                if (!TextUtils.isEmpty(username)) {
                    new FetchGitHubProfile().execute(username);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class FetchGitHubProfile extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String username = params[0];
            String[] result = new String[2];
            try {
                // Fetch user profile
                URL userUrl = new URL("https://api.github.com/users/" + username);
                HttpURLConnection userConnection = (HttpURLConnection) userUrl.openConnection();
                userConnection.setRequestProperty("Authorization", "token " + BuildConfig.GITHUB_TOKEN);
                BufferedReader userReader = new BufferedReader(new InputStreamReader(userConnection.getInputStream()));
                StringBuilder userResponse = new StringBuilder();
                String line;
                while ((line = userReader.readLine()) != null) {
                    userResponse.append(line);
                }
                userReader.close();

                // Parse user profile
                JSONObject userJson = new JSONObject(userResponse.toString());
                String name = userJson.getString("name");
                String avatarUrl = userJson.getString("avatar_url");
                result[0] = name + "|" + avatarUrl;

                // Fetch repositories
                URL reposUrl = new URL("https://api.github.com/users/" + username + "/repos");
                HttpURLConnection reposConnection = (HttpURLConnection) reposUrl.openConnection();
                reposConnection.setRequestProperty("Authorization", "token " + BuildConfig.GITHUB_TOKEN);
                BufferedReader reposReader = new BufferedReader(new InputStreamReader(reposConnection.getInputStream()));
                StringBuilder reposResponse = new StringBuilder();
                while ((line = reposReader.readLine()) != null) {
                    reposResponse.append(line);
                }
                reposReader.close();

                // Parse repositories
                JSONArray reposArray = new JSONArray(reposResponse.toString());
                List<String> repoNames = new ArrayList<>();
                List<String> repoDescriptions = new ArrayList<>();
                for (int i = 0; i < reposArray.length(); i++) {
                    JSONObject repoJson = reposArray.getJSONObject(i);
                    repoNames.add(repoJson.getString("name"));
                    repoDescriptions.add(repoJson.optString("description", "No description"));
                }
                result[1] = repoNames.toString() + "|" + repoDescriptions.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result[0] != null) {
                String[] userInfo = result[0].split("\\|");
                userName.setText(userInfo[0]);
                Glide.with(MainActivity.this).load(userInfo[1]).into(avatar);
            } else {
                Toast.makeText(MainActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }

            if (result[1] != null) {
                String[] repoInfo = result[1].split("\\|");
                List<GitHubRepo> repos = new ArrayList<>();
                String[] names = repoInfo[0].replace("[", "").replace("]", "").split(", ");
                String[] descriptions = repoInfo[1].replace("[", "").replace("]", "").split(", ");
                for (int i = 0; i < names.length; i++) {
                    GitHubRepo repo = new GitHubRepo();
                    repo.setName(names[i]);
                    repo.setDescription(descriptions[i]);
                    repos.add(repo);
                }
                RepoAdapter adapter = new RepoAdapter(MainActivity.this, repos);
                repoList.setAdapter(adapter);
            } else {
                Toast.makeText(MainActivity.this, "Failed to load repositories", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
