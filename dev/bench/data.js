window.BENCHMARK_DATA = {
  "lastUpdate": 1775724956325,
  "repoUrl": "https://github.com/ppvrflw/flint",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "name": "ppvrflw",
            "username": "ppvrflw"
          },
          "committer": {
            "name": "ppvrflw",
            "username": "ppvrflw"
          },
          "id": "44b3406263db8e57de18adf2dd3c41a944ed172a",
          "message": "run benchmark in a pr",
          "timestamp": "2026-04-09T08:36:45Z",
          "url": "https://github.com/ppvrflw/flint/pull/8/commits/44b3406263db8e57de18adf2dd3c41a944ed172a"
        },
        "date": 1775724955490,
        "tool": "jmh",
        "benches": [
          {
            "name": "me.ppvrflw.benchmark.DomainNameMatcherBenchmark.match",
            "value": 3592039.4485020544,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "me.ppvrflw.benchmark.FileHashMatcherBenchmark.match",
            "value": 8426988.227243531,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "me.ppvrflw.benchmark.Ipv4MatcherBenchmark.match",
            "value": 4188836.64908693,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          },
          {
            "name": "me.ppvrflw.benchmark.Ipv6MatcherBenchmark.match",
            "value": 2730939.0425044824,
            "unit": "ops/s",
            "extra": "iterations: 3\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}