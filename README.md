## Setting Up and Running the Parser Locally

1. Ensure you have Java and maven installed locally. You can install Java by following the instructions here:https://www.java.com/en/download/help/download_options.html. You can install maven by following the instructions here:https://maven.apache.org/install.html
2. Ensure you're in the root directory of the project. The pom.xml file should be in the root directory.
3. Run the command `mvn package`. This will create a `target` folder with the compiled classes with all the required libraries. 
4. Run the command `java -cp target/capstone-1.0.jar com.capstone.local.Main`. You should see a newly created protein_data.json file in the root directory. You can also preview this file here: https://drive.google.com/file/d/1TIa90hxYrbzzReO8T-ySrAaOG0yTppiz/view?usp=sharing