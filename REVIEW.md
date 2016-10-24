Reviewed by Ruben Gerritse

========================= Names: 

The name of the class Utils, does not really describe well what the class is about.
This should be given a more descriptive name.

---

Variables with numbers use inconsistent naming conventions. 
In the APIManager, some variables are written with an underscore (json_2016,json_2015,json_2014), however some variables are written 
without (iterator2016, iterator2015, iterator2014). This naming convention should be written consistent. Other variables without numbers
use camelcase, going with that policy json_2016,json_2015,json_2014 should be rewritten as json2016, json2015, json2014.

========================= Headers: 

No headers are present in all classes. 
For each class, a header with a short description should be written.

========================= Comments: 

Most code contain no comments. 
For example the MainActivity class has not a single comment. 
It would be better to write a short description for each function what the fuction does.

========================= Layout: 

Some lines are to long. 

See for example line 37 of the MainActivity:
public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener {

This could be rewritten as:

public class MainActivity extends AppCompatActivity implements 
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, 
        com.google.android.gms.location.LocationListener {

--- 

Commented out code is present. 
For example:
- lines 125,181-183,211 of the MainAcivity. 
- line 112 of the Authenticator class
These lines should be removed.

---

Unused import statements are present. 
Take for example the MainActivity class. 
This class contain unused import statements on the lines: 7, 19, 27, 30-35.
These statements should be removed.

---

Some functions or statements contain empty bodies.

For example the onConnectionSuspended function on lines 133-135 of the MainActivity class. 
Another example is the else-statement on line 34  of the APIManager class.

Those should be either removed, or if they are necessary fill them with code.

--- 

The variables signInButton and signOutButton in the SettingsActivity class are made global, however they are only used in one function.
They should be made locally.

========================= Formatting:  

The number of spacing between elements in the code is not always consistant, which should be.
Examples:

- In the HousingFragment class, the number of spaces between functions is 2, however in other classes this is 1.
- In the MainActivity class, after the first line declaring the class on line 37 is followed with an empty line and 
  then the variables are declared. In other classes such as APIManager, DemographicsFragment and Authenticator. This
  There is no empty line after the declaration.
- The functions saveState and loadState in the MainActivity class contain an empty line before the closing bracket. This is not the case 
  in other functions.
- The onPostExecute function in the APIManager class and the onStart function in the MainActivity class contains an empty line after 
  calling their super function. This is not the case in the onCreate function the MainActivity class.

========================= Decomposition:  
     
The function doInBackground in the APIManager is to long. Comments are showing that this function is doing different task. 
This function may be decomposed into multiple functions based on the tasks.     
