docker run -d --name dashboard -p 8182:8080 -v /path/to/local/tokens:/tokens --restart unless-stopped dashboard
docker logs -f --tail 100 dashboard