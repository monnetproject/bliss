x <- read.csv("acciones.csv")

m1 <- mean(x[x[,3]==1&x[,4]==0,1]**2)
m2 <- mean(x[x[,3]==0&x[,4]==1,1]**2)
print(m2/m1)

plot(runif(length(x[x[,3]==1&x[,4]==0,1])),x[x[,3]==1&x[,4]==0,1],col="red") # Bad 
points(runif(length(x[x[,3]==0&x[,4]==1,1])),x[x[,3]==0&x[,4]==1,1],col="blue") # Good
points(runif(length(x[x[,3]==1&x[,4]==1,1])),x[x[,3]==1&x[,4]==1,1],col="green") # Ugly (mixed)
lines(0:1,c(m1,m1),col="red")
lines(0:1,c(m2,m2),col="blue")

