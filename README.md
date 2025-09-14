# Quaderno – Shared Notes Web Application

Quaderno è un sistema di note condivise progettato come applicazione web moderna, scalabile e containerizzata.  
L’architettura si basa su microservizi con un API Gateway (Kong) e un servizio principale (`note-service`) sviluppato in Spring Boot, integrato con un frontend React.  
Il progetto è pensato per una messa in produzione reale e supporta persistenza su SQL e NoSQL, caching e ricerca full-text.

---

## 📑 Funzionalità principali
- Registrazione e autenticazione utenti (JWT).
- CRUD completo delle note (creazione, lettura, modifica, eliminazione).
- Supporto a testo semplice e hyperlink.
- Condivisione delle note con altri utenti (permesso di sola lettura).
- Ricerca e filtraggio tramite testo e tag.
- Interfaccia web intuitiva con React.

---

## 🏗️ Architettura
L’architettura prevede:
- **Kong API Gateway**: entrypoint unico, gestisce routing e sicurezza.
- **Note Service (Spring Boot)**: microservizio core che gestisce utenti, note, tag e condivisioni.  
  Integra:
    - PostgreSQL → persistenza principale.
    - Redis → caching e gestione sessioni.
    - ElasticSearch → ricerca full-text.
- **Frontend (React)**: applicazione web che comunica con il backend tramite API REST.
- **Monitoring & Health-checks**: endpoint `/management/health`, logging centralizzato e configurazioni DevOps.
- **Containerizzazione** con Docker e possibile orchestrazione con Kubernetes.

---

## 📂 Struttura repository (monorepo)

```
quaderno/
├── note-service/         # Backend Spring Boot (API REST, business logic, DB)
├── note-service
│   ├── src/main/webapp   # Frontend React (UI e interazioni utente)
├── infra/     
│   ├── k8s/              # Manifest per Kubernetes ( futura implementazione )
│   ├── helm/             # Chart Helm custom ( futura implementazione )
│   └── docker/   # Configurazioni locali (dev & prod)
└── README.md             # Questo file
```

---

## ⚙️ Requisiti
- **Java 21**
- **Node.js >= 22**
- **Docker + Docker Compose** (per sviluppo locale)

---

## 🚀 Avvio rapido in locale

### 1. Avvia i servizi esterni
```bash
cd note-service/src/main/docker
docker compose -f services.yml up -d
```
Questo avvia: PostgreSQL, Redis, ElasticSearch.

### 2. Avvia il backend
```bash
cd note-service
./mvnw spring-boot:run
```

### 3. Avvia il frontend
```bash
cd note-service/src/main/webapp
npm install
npm run start
```

Il frontend sarà disponibile su `http://localhost:9000` (configurabile).  
Il backend risponderà su `http://localhost:8080/api`.

---

## 🧪 Popolamento dati
È disponibile una **collection Postman** (`postman/Quaderno.postman_collection.json`) che crea:
- 3 utenti (`pippo/pippo123`, `pluto/pluto123`, `paperino/paperino123`).
- 10 note (alcune condivise tra più utenti).

---

## 🔒 Sicurezza
- Autenticazione con **JWT**.
- Autorizzazioni basate sui ruoli utente (owner, condiviso).
- Best practice di sicurezza: CORS configurato, input validation, rate limiting lato API Gateway.

---

## 📖 API Documentation
La documentazione API è disponibile in formato **OpenAPI/Swagger** una volta effettuata la login come admin (all’avvio, l’utente admin è `admin/admin`).

Una versione è stata scaricata e presente sotto /documentation.

---

## 📝 Limitazioni attuali
- Autenticazione gestita dal `note-service` (per semplicità); in futuro integrabile con Keycloak come Identity Provider.
- Funzionalità avanzate di monitoring (Prometheus, Grafana) non ancora integrate.
- Il frontend è ancora minimale e può essere arricchito con UX/UI più evoluta.
- Deployment su Kubernetes non ancora implementato.
- Accessibilità WCAG non ancora considerata nel frontend.
- Le note possono essere condivise solo tramite collegamento manuale a database, manca una UI per la gestione delle condivisioni.
---

## 🚀 Evoluzioni future
- Esternalizzazione della gestione utenti su Keycloak + user-service.
- Integrazione di sistemi di monitoraggio avanzati (Prometheus/Grafana).
- Deployment completo su Kubernetes gestito via Helm chart.
- Supporto accessibilità WCAG nel frontend React.

---

## 📜 Licenza
Questo progetto è distribuito con licenza **UNLICENSED** (da aggiornare in base alle policy aziendali).
