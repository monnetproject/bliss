library(Matrix)

tridiag <- function(a,b) {
  M <- diag(a)
  for(i in 1:length(b)) {
	M[i,i+1] <- b[i]
	M[i+1,i] <- b[i]
  }
  M
}

X <- matrix(c(1,1,0,0,0,1,1,1),4)
Y <- matrix(c(2,0,0,1,0,1,1,0),4)
kappa <- 0.8

K_x <- t(X) %*% X

print(K_x)

K_y <- t(Y) %*% Y

print(K_y)

I <- matrix(c(0,0,1,0,0,0,0,1,1,0,0,0,0,1,0,0),4)

B <- I %*% bdiag(K_y %*% K_x, K_x %*% K_y)

print(B)

D <- bdiag( (1-kappa) * K_x %*% K_x + kappa * K_x , (1-kappa) * K_y %*% K_y + kappa * K_y)

print(D)

Z <- eigen(solve(D) %*% B)

print(Z)

print("X")

print(X %*% Z$vectors[1:2,])

print("Y")

print(Y %*% Z$vectors[3:4,])

U <- eigen(D)$vectors

S <- eigen(D)$values ^ -1

print("U")

print(U)

print(S)

Z2 <- eigen(t(U) %*% B %*% U %*% diag(S))

print(U %*% diag(S) %*% Z2)
