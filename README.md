# Micro-futár alkalmazás
## Funkcionalitasok:
### Feladó felület
- **Csomag feladása:** Űrlap, ahol a feladó megadja a csomag adatait (méret, súly, tartalom), felvételi címet, címzett címét, szállítási opciókat, fizetési módot.
- **Árkalkuláció:** Automatikus díjszámítás a megadott paraméterek alapján.
- **Rendelés nyilvántartása:** A feladónak listában látszik minden csomagja és státusza.

### Csomagkövetés
- **Státuszok:** "Felvéve", "Átszállítva", "Futárnál", "Kiszállítva", "Sikertelen kézbesítés" . Egyedi csomagszám alapján bárki ellenőrizheti a csomag aktuális állapotát.
- **Valós idejű információk:** Térképen követhető a csomag vagy a futár helyzete.
- **Értesítések:** Email értesítések állapotváltásról.

### Logisztikai modul
- **Útvonaltervezés:** Kiszámítja a legrövidebb/leggyorsabb útvonalakat, csomagokat csoportosítja futárokhoz optimalizálás szerint.
- **Futár beosztás:** Automatikus vagy manuális csomag-hozzárendelés futárhoz, figyelembe véve kapacitást, elérhetőséget, területet.
- **Raktárkezelés:** Ha több raktár van, azok közti mozgatás kezelése.

### Futár felület
- **Feladatlista:** Rangsorolt listában látja, mely címekre kell menni, a prioritást logisztika határozza meg.
- **Navigáció:** Térképes útvonaltervezővel kijelölt cím, időbecslés.
- **Állapotkezelés:** Minden csomagnál beállítható aktuális státusz, például "Felvéve", "Kézbesítve", "Nem sikerült átadni".
- **Értesítések:** Automatikus kommunikáció a címzett/feladó felé kézbesítési státuszról.

***

## Architektúra
A csomagszállító alkalmazás funkcióit jól el lehet különíteni egy **microservice architektúrában**; minden fő modul külön mikroszolgáltatásként jelenik meg, önálló API-val.

### Frontend
A csomagszállító alkalmazás teljes microservice architektúrája modulonként szétosztva, dedikált frontendelemekkel a felhasználótípusok és üzleti funkciók szerint épül fel.

- **Feladói/Követési UI:**  
  Web/mobil alkalmazás a feladók, címzettek számára csomagfeladásra és -követésre  
  Backend-for-Frontend (BFF): csak a feladói/követési UI-hoz kiszolgáló saját REST API.
- **Logisztikai UI:**  
  Admin/járatszervező felület logisztikusoknak: csomag-kiosztás, útvonaltervezés, raktárkezelés  
  BFF: csak logisztikai UI-t szolgálja ki, logisztikai backendnek proxyzik.
- **Futár UI:**  
  Mobil/web felület futároknak: rangsorolt címek, navigáció, státuszkezelés  
  BFF: a futárokhoz tartozó adatok és endpointok, jogosultság szerinti adat-szűrés.

### Backend 

- **Csomagfeladás szolgáltatás:**  
  Csomag adatok, árkalkuláció, megrendelés-nyilvántartás, státuszváltások.
- **Csomagkövetés szolgáltatás:**  
  Státusztranzakciók, nyomonkövetés, értesítések, csomaginformációk valós idejű megjelenítése.
- **Logisztika szolgáltatás:**  
  Útvonal- és járatszervezés, futárokhoz rendelés, priorizálás, raktári mozgatás.
- **Futár szolgáltatás:**  
  Futár profilok, beosztások, feladatlista, státuszmódosítás.

### 3. Központi modul

- **Felhasználókezelő/Auth Service:**  
  Jogosultságok, profilok, regisztráció, login.
- **API Gateway:**  
  Központi belépési pont, routing az egyes szolgáltatásokhoz.

