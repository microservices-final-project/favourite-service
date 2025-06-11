# FavouriteApi 

prefijo `/favourite-service`

Obtener todos los favoritos

GET `/api/favourites`

Funciona bien, falta arregla que si se borrar un usuario o producto que no corte el stream


GET `/api/favourites/{userId}/{productId}`

Funciona bien, falta arregla que si se borrar un usuario o producto que diga porque, pide la fecha lo cual no tiene sentido

Crear favorito

POST `/api/favourites`

No tiene sentido que pida la fecha si eso es algo que se debe obtener del momento

Eliminar favorito

DELETE `/api/favourites`

No tiene sentido que pida la fecha, el usuario nunca se va a acordar de eso
