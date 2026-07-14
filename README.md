"# ShopBackend" 

1. Install Docker SQL nya :		
		
	docker run -p 3307:3306 --name mysqlcontainer -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=shop_db -d mysql:latest
	
 2. Cek Image nya

		docker ps

3. Jalanin Maven nya
		mvn clean install
		mvn clean compile -DskipTests

4. jalanin web nya

		mvn spring-boot:run
		
		
5. Masuk database 

    docker exec -it mysqlcontainer mysql -u root -p
		
6. cek table 
    
    "USE shop_db;
    SELECT * FROM users;"
		
7. cek login	

    curl -X POST http://localhost:8080/api/auth/verify-password -H "Content-Type: application/json" -d "{\"email\":\"user@mail.com\",\"password\":\"123456\"}"
		
	
8. daftar

    curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"email\":\"user@mail.com\",\"password\":\"123456\",\"name\":\"Budi\"}"

