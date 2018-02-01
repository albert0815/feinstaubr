# TODOs
- Include version in build
- create backup job for db (see https://stackoverflow.com/questions/24718706/backup-restore-a-dockerized-postgresql-database)
- last 24h in sensor bean, include type and id filter
- include other types in web page
- make it responsive
- activate gzip for json
- ladebalken
- only update if focus
- landscape iphone niht unbedingt zwei nebeneinander
- durchschnitt / höchst / niedrigstwert
- daten von ajax speichern für redraw der diagramme bei resize
- build prozess unterschied in persistence.xml
- add sensor id to hash
- setup google container
- fehler wenn sensor nicht da ist
- anzeige der diagramme/current nur wenn daten da sind (material motion slide in)
- return all in one json

# create google cluster
https://cloud.google.com/kubernetes-engine/docs/tutorials/persistent-disk?hl=de
https://codelabs.developers.google.com/codelabs/cloud-persistent-disk/index.html?index=..%2F..%2Findex#7

gcloud container clusters get-credentials feinstaubr-cluster --zone europe-west3-a
gcloud container clusters list
gcloud compute disks create --size 200GB postgres-disk
kubectl create -f postgres.yaml
gcloud compute instances attach-disk cluster-1 --disk postgres-disk
gcloud compute ssh gke-cluster-1-default-pool-39c0ef9a-pd0v
gcloud config get-value project -q
docker build -t gcr.io/feinstaubr/feinstaubr-web:1.0.0 .
gcloud docker -- push gcr.io/feinstaubr/feinstaubr-web:1.0.0
docker push gcr.io/feinstaubr/feinstaubr-web:1.0.0
gcloud compute addresses create feinstaubr-ip --global
gcloud compute addresses describe feinstaubr-ip
=> address: 35.186.229.96

kubectl create -f kubernetes-feinstaubr-deployment.yaml


psql -U postgres --password -f c:\temp\feinstaubr.sql

C:\temp>
cloud_sql_proxy_x64 -instances=feinstaubr:europe-west3:feinstaubr-db=tcp:5432 -credential_file=C:\Users\papend\AppData\Local\Temp\Feinstaubr-c5b048be0f3a.json

# connect to db
https://cloud.google.com/sql/docs/postgres/connect-container-engine
1. dienst konto anlegen
2. credentials in gcloud anlegen
3. credentials für kubernetes anlegen:
kubectl create secret generic cloudsql-instance-credentials --from-file=credentials.json=C:\Users\papend\AppData\Local\Temp\Feinstaubr-c5b048be0f3a.json
kubectl create secret generic cloudsql-db-credentials --from-literal=username=proxyuser --from-literal=password=nYgSmYSeyQnctlcC16xO


https://cloud.google.com/solutions/continuous-delivery-with-travis-ci