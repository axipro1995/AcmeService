# AcmeService
TEKO test: Webservices for seat reservation of ACME.

## Start
```
./run.sh
```
The services will listen on port 8080.

## Configuration
```
cat ./run.sh
```
- ```-Dcols```: number of columns.
- ```-Drows```: number of rows.
- ```-Dspace```: acceptable distance.
- ```-DfilePath```: data stored file path. Optional.

## Testing
- get available seats: ```curl -X GET -i 'http://localhost:8080/getAvailable?quantity=3'```
- do reservation seats: ```curl -X PUT -H 'Content-Type: application/json' -i http://localhost:8080/takeSeat --data '[{"x":10,"y":3},{"x":10,"y":4}]'```

## Structure
```
|---src                 					: source code directory
	|---main 						: main source code
		|---java 							
			|---com.dungns.logic 			: logical package
			|---com.dungns.restservice 		: web services controller package
		|---resource 					: project's resource. Empty.
	|---test 						: test source code. Empty.
|---target 							: built libraries.
|---diagrams 							: application's flow descriptions.
|---data.txt 							: data stored file.
|---run.sh 							: application runner.
```
