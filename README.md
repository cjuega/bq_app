# DropboxLibrary #

Android app that connects to Dropbox accounts and use them as book libraries.

Currently there are two folders available:

* app: which contains the app itself.

* test: which contains an Android test project to check the app's correctness.


## External tools ##

This approach uses two external libraries:

* The [dropbox sync API](https://www.dropbox.com/developers/sync) to connect to Dropbox accounts and manage dropbox files.

* The [epublib](http://www.siegmann.nl/epublib/android) library to read .epub files.

## El caso ##

La empresa bq tiene una dilatada experiencia en soluciones de lectura electrónica. Desde dispositivos de tinta electrónica a aplicaciones de lectura tanto en IOS como en Android.

Cada día más clientes piden poder sincronizar, tanto los dispositivos como las aplicaciones, con servicios de almacenamiento en la nube como Dropbox o Google Drive.

El caso que proponemos al candidato o candidata es el siguiente:

Desarrollar una aplicación Android que trate la cuenta de usuario de dropbox como una biblioteca de libros remota, de forma que:

1. Exista una pantalla inicial de login, donde el usuario pueda introducir sus credenciales para tener acceso a su cuenta Dropbox. (https://www.dropbox.com/)

2. Una vez introducidos los credenciales, se mostrarán en pantalla todos los libros que el usuario disponga en su cuenta Dropbox en forma de biblioteca. Esto es, todos los ficheros que tengan extensión .epub . (Para representar cada libro, puede usarse un icono genérico).

3. Dicha pantalla tendrá un menú desplegable con dos opciones, una de ellas ordenará la lista de libros por el nombre del archivo y la otra por fecha de creación. (Idealmente nos gustaría ordenar por el título del libro en vez del nombre del fichero)

4.  Si el usuario clickea dos veces sobre el icono genérico que representa cada libro, la aplicación mostrará la portada del libro clickEado.


NO se espera que el candidato sea capaz de realizar los cuatro puntos en un periodo tan corto de tiempo, por lo que podrá saltarse o crear un mock de aquello que considere oportuno.
Se pide al candidato que nos haga llegar la aplicación a traves de github.com, haciéndonos llegar su URL tan pronto haya sido creada y a ser posible desde que comience a implementar la aplicación, para así poder seguir los commits del candidato desde el primer minuto.
Se pide al candidato que documente como ha ido realizando todos y cada uno de los pasos incluso los que no ha podido hacer, ya sea por motivos ajenos o propios.
Se pide al candidato que NO preste especial atención a la calidad de las interfaces gráficas que diseña, ya que está fuera del alcance de esta candidatura.

Gracias.
