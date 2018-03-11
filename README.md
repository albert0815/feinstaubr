# TODOs
- activate gzip for json
- landscape iphone niht unbedingt zwei nebeneinander
- build prozess unterschied in persistence.xml
- fehler wenn sensor nicht da ist
- anzeige der diagramme/current nur wenn daten da sind (material motion slide in)
- switch to google build https://cloud.google.com/container-builder/docs/how-to/build-triggers
- compare values stored in luftdaten.info with mine
- adjust build for branch different than master
- scroll to previous / next day
- include google cloud trace / google cloud logging

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
=> address: 35.227.205.81

kubectl create -f kubernetes-feinstaubr-deployment.yaml


psql -U postgres --password -f c:\temp\feinstaubr.sql
pg_dump -U postgres --password feinstaub>dump

# ssl

apt-get update  && apt-get -y install certbot
certbot certonly --webroot -w /usr/share/nginx/html -d feinstaub.dirkpapenberg.de

https://runnable.com/blog/how-to-use-lets-encrypt-on-kubernetes
kubectl create secret tls letsencrypt-cert --cert=fullchain.pem --key=tls.key

status batch: geht nicht, certificate konnte zwar ausgestellt werden, aber secret wurde nicht aktualisiert. job nach kube-systme geschoben

C:\temp>
cloud_sql_proxy_x64 -instances=feinstaubr:europe-west3:feinstaubr-db=tcp:5432 -credential_file=C:\Users\papend\AppData\Local\Temp\Feinstaubr-c5b048be0f3a.json
cloud_sql_proxy_x64 -instances=feinstaubr:europe-west3:feinstaubr-db=tcp:8765 -credential_file=C:\Users\dpape\Downloads\Feinstaubr-5461397f5be3.json

# connect to db
https://cloud.google.com/sql/docs/postgres/connect-container-engine
1. dienst konto anlegen
2. credentials in gcloud anlegen
3. credentials f�r kubernetes anlegen:
kubectl create secret generic cloudsql-instance-credentials --from-file=credentials.json=C:\Users\papend\AppData\Local\Temp\Feinstaubr-c5b048be0f3a.json
kubectl create secret generic cloudsql-db-credentials --from-literal=username=proxyuser --from-literal=password=nYgSmYSeyQnctlcC16xO


https://cloud.google.com/solutions/continuous-delivery-with-travis-ci


# change commiter
git filter-branch --commit-filter 'export GIT_COMMITTER_NAME="albert0815";export GIT_AUTHOR_NAME="albert0815"; export GIT_AUTHOR_EMAIL=mail@example.com;export GIT_COMMITTER_EMAIL=mail@example.com; git commit-tree "$@"'


#licenses
usage of material design icons from google as per apache 2 license (https://github.com/google/material-design-icons) 
