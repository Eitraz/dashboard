docker run -d --name dashboard -p 8182:8080 --restart unless-stopped dashboard
docker logs -f --tail 100 dashboard