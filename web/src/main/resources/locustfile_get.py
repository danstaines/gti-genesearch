from locust import HttpLocust, TaskSet, task
import random


urls = ["/api/genes/query?q={\"name\":\"BRCA2\"}", "/api/health", "/api/genes/info",
        "/api/genes/query?query={\"name\":\"BRCA2\",\"lineage\":\"40674\"}",
        "/api/genes/query?query={\"GO_expanded\":\"GO:0016787\"}",
        "/api/genes/query?query={\"GO\":{\"evidence\":\"IMP\",\"term\":\"GO:0006302\"}}",
        "/api/genes/query?query={\"PHI\":{\"host\":\"4565\",\"phenotype\":\"reduced virulence\"}}",
        "/api/genes/query?query={\"Uniprot/SWISSPROT\":[\"P03886\"]}",
        "/api/genes/query?query={\"Uniprot/SWISSPROT\":[\"P03886\",\"P03891\",\"P00395\",\"P00403\",\"P03928\"]}",
        "/api/genes/query?query={\"genome\":\"homo_sapiens\",\"start\":\"45000-46000\"}",
        "/api/genes/query?query={\"genome\":\"homo_sapiens\",\"start\":\">45000\"}",
        "/api/genes/query?query={\"genome\":\"homo_sapiens\",\"start\":\"<=45000\"}",
        "/api/genes/query?query={\"genome\":\"homo_sapiens\",\"location\":\"1:45000-96000\"}",
        "/api/genes/query?query={\"genome\":\"homo_sapiens\",\"location\":\"1:45000-96000:-1\"}",
        "/api/genes/query?query={\"genome\":\"homo_sapiens\",\"location\":[\"1:45000-52000\",\"1:60000-96000\"]}"        
        "/api/genes/query?query={\"name\":\"BRCA2\"}&fields=genome,name,description,start,end",
        "/api/genes/query?query={\"name\":\"BRCA2\"}&fields=genome,name,description,start,end&array=true",
        "/api/genes/query?query={\"name\":\"BRCA2\"}&fields=genome,name,description,start,end,transcripts.name,transcripts.biotype",
        "/api/genes/query?query={\"name\":\"BRCA2\",\"genome\":\"homo_sapiens\"}&fields=[\"name\",\"description\",{\"homologues\":[\"division\",\"name\"]}]",
        "/api/genes/query?query={\"name\":\"BRCA2\",\"genome\":\"homo_sapiens\",\"homologues\":{\"lineage\":\"9443\"}}&fields=[\"name\",\"description\",{\"homologues\":[\"division\",\"name\"]}]",
        "/api/genes/query?query={\"name\":\"BRCA2\",\"genome\":\"homo_sapiens\"}&fields=[\"name\",\"description\",{\"variants\":[\"id\",\"chr\",\"start\",\"stop\"]}]",
        "/api/genes/query?query={\"name\":\"BRCA2\",\"genome\":\"homo_sapiens\",\"variants\":{\"inner\":1}}&fields=[\"name\",\"description\",{\"variants\":[\"id\",\"chr\",\"start\",\"stop\"]}]",
        "/api/genes/query?query={\"name\":\"BRCA2\",\"genome\":\"homo_sapiens\"}&fields=[\"name\",\"description\",{\"variants\":[\"count\"]}]",
        "/api/genes/query?query={\"name\":\"BRCA2\",\"genome\":\"homo_sapiens\",\"variants\":{\"annot\":{\"ct\":{\"so\":\"1627\"}}}}&fields=[\"name\",\"description\",{\"variants\":[\"id\",\"chr\",\"start\",\"stop\"]}]",
        "/api/genes/query?query={\"name\":\"BRCA2\",\"genome\":\"homo_sapiens\"}&fields=[\"name\",\"description\",{\"expression\":[\"experimentType\",\"expressionLevel\"]}]",
        "/api/genes/query?query={\"name\":\"BRCA2\"}&facets=genome",
        "/api/genes/query?query={\"name\":\"BRCA2\"}&facets=genome",
        "/api/genes/query?query={\"GO_expanded\":\"GO:0006302\"}&fields=genome,name,start,end&sort=genome",
        "/api/genes/query?query={\"GO_expanded\":\"GO:0006302\"}&fields=genome,name,start,end&sort=genome,start",
        "/api/genes/query?query={\"GO_expanded\":\"GO:0006302\"}&fields=genome,name,start,end&sort=genome,-start"
        ]

class UserBehavior(TaskSet):
    
    def on_start(self):
        """ on_start is called when a Locust start before any task is scheduled """

    @task(1)
    def profile(self):
        self.client.get(random.choice(urls))

class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    min_wait = 5000
    max_wait = 9000
