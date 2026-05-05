
# Common Ground

HOW TO USE THIS SOFTWARE:

Common Ground is a acessibility QnA platform designed for shared experiences and understanding.

If you recieved/downloaded this as a singleton, you likely only have this file, a folder named build, and a couple .sh scripts.
The build folder contains all of the files you need to run this as a singleton on any machine with Docker installed. 

There are 4 scripts you need to be familair with.

1. install.sh: Unpacks the contents of the build folder into your Docker repository, and also pulls out a few files it needs for operation.
2. run.sh: Once installed, this will start the Docker Compose project, including the outward facing Nginx layer, the web service, the database, and the Spring backend.
3. stop.sh: This stops the running Docker Compose project.
4. clean.sh: This cleans up the installed container images, but does NOT remove the files in this folder it created. After running clean, it is safe to delete the folder this came from, this is necessary for updates.

To Install:

* Configure Docker for your machine
* Copy the contents of the common-ground folder off of the disc or drive.
* Grant execute permissions to install.sh
* Run install.sh
