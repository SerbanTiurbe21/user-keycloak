# Microserviciul User-Keycloak 

Microserviciul User-Keycloak servește ca serviciu central pentru gestionarea utilizatorilor și autentificarea în cadrul sistemului de microservicii. Acesta implementează mecanisme robuste de securitate prin utilizarea Keycloak, un open source Identity and Access Management.

## Descriere

Serviciul User Service este implementat folosind Spring Boot și Keycloak, oferind un layer robust de gestionare a utilizatorilor și securitate. Acesta facilitează autentificarea și autorizarea utilizatorilor prin integrare cu Keycloak și include mecanisme avansate pentru gestionarea situațiilor de eroare și validare.

## Configurarea Proiectului

User-Keycloak este configurat printr-un set de reguli definite în fișierul application.yaml care include:
  - Setări pentru conectarea la Keycloak
  - Setări de securitate pentru validarea JWT tokens
  - Configurarea portului pe care rulează serviciul

## Dockerfile

Proiectul include un `Dockerfile` pentru containerizarea și desfășurarea ușoară a serviciului User-Keycloak. Acesta este configurat pentru a rula pe portul 8085.

## Rularea Microserviciului cu Docker

Pentru a rula serviciul User Service într-un container Docker, urmează pașii simpli de mai jos pentru a construi și rula imaginea.

### Construirea Imaginii Docker

  - Deschide un terminal și navighează în directorul sursă al proiectului User Service, unde se află `Dockerfile`.
  - Rulează următoarea comandă pentru a construi imaginea Docker pentru User Service. Acest pas va crea o imagine Docker locală etichetată ca userkeycloak-service:

    `docker build -t userkeycloak-service .`

### Rularea Containerului Docker

După construirea imaginii, poți rula containerul folosind imaginea creată:

`docker run -p 8085:8085 userkeycloak-service`

Această comandă va porni un container din imaginea user-service, mapând portul 8085 al containerului pe portul 8085 al mașinii tale locale. Asta înseamnă că poți accesa User Service navigând la http://localhost:8085 în browserul tău. 

:bangbang: Însă acest pas nu este necesar pentru că există un `Dockerfile` în repository-ul central de unde se vor porni toate containerele. :bangbang:
