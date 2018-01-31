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

gcloud container clusters get-credentials cluster-1 --zone us-central1-a
gcloud container clusters list
gcloud compute disks create --size 200GB postgres-disk
kubectl create -f postgres.yaml
gcloud compute instances attach-disk cluster-1 --disk postgres-disk
gcloud compute ssh gke-cluster-1-default-pool-39c0ef9a-pd0v