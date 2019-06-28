# SearchFilter

## Modules
* Backend: The backend is implemented as a Tomcat web container. The
  frontend needs to be build first.
* Common: Contains common modules
* Frontend: The frontend is a webpack web project
* Preprocessing: The preprocessing module

## Configuration
We use a central properties configuration file. The file's location is:

```Backend/src/main/resources/config.properties```

## Building
We use the Maven and NPM build systems. To build the system for the 
first time:

1. Build the frontend: ```npm run build```
2. Build the Common module: ```mvn install```
3. Build the Backend and Preprocessing modules: ```mvn package```


## How to run
### Preprocessing
From the Preprocessing folder run:

```java -jar target/preprocessing-$VERSION.jar ../Backend/src/main/resources/config.properties```

### Deployment
Put the Lucene index to some location accessible by Tomcat (set the 
permissions appropriately) and deploy the web archive (usually the path to 
the index needs to be adapted)
