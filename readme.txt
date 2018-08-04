/**************************************************
Location of SVN
**************************************************/
https://git-teaching.cs.bham.ac.uk/mod-ug-proj-2017/dxa523/

/****************************************
* Libraries - Need to be in lib folder
****************************************/
jackson-annotations-2.9.4.jar
jackson-core-2.9.4.jar
jackson-databind-2.9.4.jar
mysql-connector-java-5.1.45-bin.jar
stanford-corenlp-3.8.0-javadoc.jar
stanford-corenlp-3.8.0-models.jar
stanford-corenlp-3.8.0-sources.jar
stanford-corenlp-3.8.0.jar

/***************************************************
Set up
***************************************************/
To run the program, Java-9, Mysql-5.7 and Python are needed
These can be installed by following the instructions below:

 - Java - 
/* Execute the commands below in the command terminal: */

sudo apt-get update;
sudo apt-get install default-jre;
sudo apt-get install default-jdk;
sudo add-apt-repository ppa:webupd8team/java;
sudo apt-get update;
sudo apt-get install oracle-java9-installer;

 - MYSQL - 
sudo apt-get update;
sudo apt-get install mysql-*;

/* If necessary, you can add a user to your mysql sql installation by first connecting
do you mysql server and running the following command, replacing the username
and password as appropriate: */
GRANT ALL PRIVILEGES ON *.* TO 'username'@'localhost' IDENTIFIED BY 'password';

- Python -
/* Execute the following commands in the terminal. The alias is command is only
temporary, it is recommonded you permanently change your defauly python version
to python 3 if not already done. */
sudo apt-get install python3
sudo apt-get install python3-pip
sudo pip3 install -U nltk
sudo pip install -U numpy
alias python=python3

/* Run python in the terminal */
import nltk
nltk.download()

/* download all of the nltk packages

/*****************************************************
Compile
*****************************************************/
/* Execute the command */

javac -cp :src/:src/libs/* src/windows/Main.java

/***************************************************
Run
/*******************************************************
/* Execute the command */
java -cp src/libs/*:src/ windows.Main
