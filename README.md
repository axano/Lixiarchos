# Lixiarchos

## Install
Although Windows is supported, we advise using a Unix-based operating system for better compatibility and performance.

To install Lixiarchos, follow these steps:
 

1. install mysql/mariadb
   1. sudo apt install mariadb-server2.
   2. sudo mysql_secure_installation
2. Create lixiarchos database
   1. sudo mysql -u root -p
   2. CREATE DATABASE lixiarchos;
3. Install Latest Java JDK (version 25 or higher)
   1. apt-get install default-jdk
4. adjust application.properties with:
   1. your database credentials, ports etc.
   2. admin user and password
5. Build the project with maven
   1. mvn clean install
6. Run the application
   1. java -jar lixiarchos.war
   2. java -jar lixiarchos.war --spring.profiles.active=prod (use if you have multiple profiles )
7. Access the application
   1. Open your web browser and navigate to http://localhost



## Deamonize Lixiarchos
To run Lixiarchos as a service on a Unix-based system, you can create a systemd service file. Here’s how to do it:
1. Create a service file:
   1. sudo nano /etc/systemd/system/lixiarchos.service
   2. Add the following content to the file, adjusting the paths as necessary:
      ```
      [Unit]
      Description=Lixiarchos Service
      After=network.target
      [Service]
      User=www-data
      ExecStart=/usr/bin/java -jar /opt/lixiarchos/Lixiarchos.war --spring.profiles.active=prod
      SuccessExitStatus=143
      Restart=on-failure
      RestartSec=10
      Environment=JAVA_OPTS=-Xms512m -Xmx1024m
      StandardOutput=append:/var/log/lixiarchos.log
      StandardError=append:/var/log/lixiarchos.log
      
      [Install]
      WantedBy=multi-user.target
      ```
   3. Save and close the file.
   4. Reload systemd to recognize the new service and start it:
         ```
      sudo systemctl daemon-reload
      sudo systemctl enable lixiarchos
      sudo systemctl start lixiarchos
      ```

## Restoring backups
If restoring a backup, pulled through the admin panel, and you are moving from Mysql to MariaDB or vice versa, you may need to adjust the sql file by replacing all occurrences of utf8mb4_0900_ai_ci with utf8mb4_unicode_520_ci

## TODO 

- dockerize the application for easier deployment
- make input validation more robust
