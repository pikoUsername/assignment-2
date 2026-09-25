### Workload 1 - Random access

| Structure | Operation | n | m | Avg time (ms) | ns / op | Accesses | per op | Theory |
|---|---|---:|---:|---:|---:|---:|---:|---|
| DynamicArray | get | 100 | 10,000 | 0.009 | 0.9 | 10,000 | 1.0 | Theta(1) per get |
| DynamicArray | get | 1,000 | 10,000 | 0.158 | 15.8 | 10,000 | 1.0 | Theta(1) per get |
| DynamicArray | get | 10,000 | 10,000 | 0.045 | 4.5 | 10,000 | 1.0 | Theta(1) per get |
| DynamicArray | get | 100,000 | 10,000 | 0.017 | 1.7 | 10,000 | 1.0 | Theta(1) per get |
| LinkedList | get | 100 | 10,000 | 0.304 | 30.4 | 511,327 | 51.1 | Theta(n) per get |
| LinkedList | get | 1,000 | 10,000 | 4.167 | 416.7 | 5,021,262 | 502.1 | Theta(n) per get |
| LinkedList | get | 10,000 | 10,000 | 43.786 | 4378.6 | 50,188,951 | 5018.9 | Theta(n) per get |
| LinkedList | get | 100,000 | 10,000 | 445.384 | 44538.4 | 504,940,938 | 50494.1 | Theta(n) per get |

### Workload 2 - Search

| Structure | Operation | n | m | Avg time (ms) | ns / op | Comparisons | per op | Theory |
|---|---|---:|---:|---:|---:|---:|---:|---|
| DynamicArray | contains | 100 | 1,000 | 0.050 | 50.0 | 75,375 | 75.4 | Theta(n) per search |
| DynamicArray | contains | 1,000 | 1,000 | 0.451 | 451.1 | 750,836 | 750.8 | Theta(n) per search |
| DynamicArray | contains | 10,000 | 1,000 | 4.133 | 4133.3 | 7,529,336 | 7529.3 | Theta(n) per search |
| DynamicArray | contains | 100,000 | 1,000 | 32.786 | 32786.4 | 73,835,543 | 73835.5 | Theta(n) per search |
| LinkedList | contains | 100 | 1,000 | 0.108 | 108.3 | 75,375 | 75.4 | Theta(n) per search |
| LinkedList | contains | 1,000 | 1,000 | 0.837 | 836.7 | 750,836 | 750.8 | Theta(n) per search |
| LinkedList | contains | 10,000 | 1,000 | 6.775 | 6774.9 | 7,529,336 | 7529.3 | Theta(n) per search |
| LinkedList | contains | 100,000 | 1,000 | 66.432 | 66432.1 | 73,835,543 | 73835.5 | Theta(n) per search |

### Workload 3 - Insertion and removal

| Structure | Operation | n | m | Avg time (ms) | ns / op | Accesses + moves | per op | Theory |
|---|---|---:|---:|---:|---:|---:|---:|---|
| DynamicArray | insert_front | 100 | 1,000 | 0.058 | 57.6 | 602,420 | 602.4 | Theta(n) shifts per op |
| DynamicArray | insert_front | 1,000 | 1,000 | 0.196 | 195.7 | 1,501,524 | 1501.5 | Theta(n) shifts per op |
| DynamicArray | insert_front | 10,000 | 1,000 | 1.164 | 1163.8 | 10,500,500 | 10500.5 | Theta(n) shifts per op |
| DynamicArray | insert_front | 100,000 | 1,000 | 7.638 | 7638.4 | 100,500,500 | 100500.5 | Theta(n) shifts per op |
| LinkedList | insert_front | 100 | 1,000 | 0.004 | 4.2 | 1,000 | 1.0 | Theta(1) per op |
| LinkedList | insert_front | 1,000 | 1,000 | 0.007 | 7.0 | 1,000 | 1.0 | Theta(1) per op |
| LinkedList | insert_front | 10,000 | 1,000 | 0.007 | 7.0 | 1,000 | 1.0 | Theta(1) per op |
| LinkedList | insert_front | 100,000 | 1,000 | 0.005 | 5.5 | 1,000 | 1.0 | Theta(1) per op |
| DynamicArray | remove_front | 100 | 100 | 0.002 | 23.6 | 5,050 | 50.5 | Theta(n) shifts per op |
| DynamicArray | remove_front | 1,000 | 1,000 | 0.040 | 40.1 | 500,500 | 500.5 | Theta(n) shifts per op |
| DynamicArray | remove_front | 10,000 | 1,000 | 1.075 | 1074.5 | 9,500,500 | 9500.5 | Theta(n) shifts per op |
| DynamicArray | remove_front | 100,000 | 1,000 | 6.423 | 6423.1 | 99,500,500 | 99500.5 | Theta(n) shifts per op |
| LinkedList | remove_front | 100 | 100 | 0.000 | 2.0 | 200 | 2.0 | Theta(1) per op |
| LinkedList | remove_front | 1,000 | 1,000 | 0.002 | 2.2 | 2,000 | 2.0 | Theta(1) per op |
| LinkedList | remove_front | 10,000 | 1,000 | 0.003 | 2.5 | 2,000 | 2.0 | Theta(1) per op |
| LinkedList | remove_front | 100,000 | 1,000 | 0.003 | 2.9 | 2,000 | 2.0 | Theta(1) per op |
| DynamicArray | insert_middle | 100 | 1,000 | 0.035 | 34.9 | 302,920 | 302.9 | Theta(n) shifts per op |
| DynamicArray | insert_middle | 1,000 | 1,000 | 0.109 | 109.1 | 752,024 | 752.0 | Theta(n) shifts per op |
| DynamicArray | insert_middle | 10,000 | 1,000 | 0.457 | 456.9 | 5,251,000 | 5251.0 | Theta(n) shifts per op |
| DynamicArray | insert_middle | 100,000 | 1,000 | 3.831 | 3831.3 | 50,251,000 | 50251.0 | Theta(n) shifts per op |
| LinkedList | insert_middle | 100 | 1,000 | 0.264 | 264.2 | 301,500 | 301.5 | Theta(n) traversal per op |
| LinkedList | insert_middle | 1,000 | 1,000 | 0.741 | 740.5 | 751,500 | 751.5 | Theta(n) traversal per op |
| LinkedList | insert_middle | 10,000 | 1,000 | 4.790 | 4790.2 | 5,251,500 | 5251.5 | Theta(n) traversal per op |
| LinkedList | insert_middle | 100,000 | 1,000 | 44.932 | 44932.5 | 50,251,500 | 50251.5 | Theta(n) traversal per op |
| DynamicArray | remove_middle | 100 | 100 | 0.006 | 56.8 | 2,550 | 25.5 | Theta(n) shifts per op |
| DynamicArray | remove_middle | 1,000 | 1,000 | 0.040 | 39.6 | 250,500 | 250.5 | Theta(n) shifts per op |
| DynamicArray | remove_middle | 10,000 | 1,000 | 0.275 | 275.0 | 4,750,500 | 4750.5 | Theta(n) shifts per op |
| DynamicArray | remove_middle | 100,000 | 1,000 | 3.164 | 3163.6 | 49,750,500 | 49750.5 | Theta(n) shifts per op |
| LinkedList | remove_middle | 100 | 100 | 0.002 | 16.2 | 2,700 | 27.0 | Theta(n) traversal per op |
| LinkedList | remove_middle | 1,000 | 1,000 | 0.245 | 244.9 | 252,000 | 252.0 | Theta(n) traversal per op |
| LinkedList | remove_middle | 10,000 | 1,000 | 4.225 | 4225.3 | 4,752,000 | 4752.0 | Theta(n) traversal per op |
| LinkedList | remove_middle | 100,000 | 1,000 | 43.561 | 43561.1 | 49,752,000 | 49752.0 | Theta(n) traversal per op |

### Workload 4 - Priority processing

| Structure | Operation | n | m | Avg time (ms) | ns / op | Comparisons | per op | Theory |
|---|---|---:|---:|---:|---:|---:|---:|---|
| MinHeap | insert | 100 | 100 | 0.001 | 6.6 | 194 | 1.9 | O(log n) per op; O(n log n) total |
| MinHeap | insert | 1,000 | 1,000 | 0.015 | 15.4 | 2,232 | 2.2 | O(log n) per op; O(n log n) total |
| MinHeap | insert | 10,000 | 10,000 | 0.127 | 12.7 | 22,593 | 2.3 | O(log n) per op; O(n log n) total |
| MinHeap | insert | 100,000 | 100,000 | 1.400 | 14.0 | 227,662 | 2.3 | O(log n) per op; O(n log n) total |
| MinHeap | peekMin | 100 | 10,000 | 0.000 | 0.0 | 0 | 0.0 | Theta(1) per op |
| MinHeap | peekMin | 1,000 | 10,000 | 0.011 | 1.1 | 0 | 0.0 | Theta(1) per op |
| MinHeap | peekMin | 10,000 | 10,000 | 0.000 | 0.0 | 0 | 0.0 | Theta(1) per op |
| MinHeap | peekMin | 100,000 | 10,000 | 0.020 | 2.0 | 0 | 0.0 | Theta(1) per op |
| MinHeap | extractMin | 100 | 100 | 0.001 | 10.8 | 841 | 8.4 | O(log n) per op; Theta(n log n) total |
| MinHeap | extractMin | 1,000 | 1,000 | 0.047 | 47.1 | 14,994 | 15.0 | O(log n) per op; Theta(n log n) total |
| MinHeap | extractMin | 10,000 | 10,000 | 0.552 | 55.2 | 216,736 | 21.7 | O(log n) per op; Theta(n log n) total |
| MinHeap | extractMin | 100,000 | 100,000 | 6.230 | 62.3 | 2,831,463 | 28.3 | O(log n) per op; Theta(n log n) total |
