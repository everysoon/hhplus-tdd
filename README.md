# JAVA에서의 TDD를 사용한 동시성 제어 간단 프로젝트 

## 동시성
: 여러 개의 프로세스나 스레드가 동시에 실행되는 환경을 의미

자바에서는 멀티 스레딩을 지원하여 여러 스레드가 동시에 작업을 할 수 있는데, 이러한 멀티 스레딩 환경에서 여러 스레드가 **동일 리소스**에 접근할 때의 문제를 해결하기 위해
분산 환경을 고려하지 않은, 단일 환경에서의 syncronized, ReentrantLock, Automic 연산을 비교해 보았습니다.

## 프로젝트 설명
본 프로젝트에서는 데이터 베이스를 사용하지 않고 간단한 데이터 베이스의 기능을 모방하는 UserPointTable.class, PointHistoryTable.class Map을 사용했습니다. (in /database 폴더)
그 후 간단한 point 충전, 사용, 포인트 조회, 포인트 이력 조회에 관한 네가지 API를 구현하였습니다.

포인트 충전, 사용을 할 때 동시성 제어를 위해 ReentrantLock을 사용하였는데 왜 ReentrantLock을 선택하였고 나머지의 장/단점은 무엇인지 세 가지를 비교하는 문서를 작성하고자 합니다.

### 1) Syncronized
Syncronized는 자바에서 동시성 제어를 위해 가장 기본적으로 메서드나 코드블록에 사용할 수 있는 키워드로 제공하고있는 방법입니다.
```
public syncronized void increment(){
  count++;
}
```
- 이런식으로 간단하게 메서드에 작성해주는 것 만으로도 동시성 제어를 할 수 있으며 추가 설정이 필요 없다는 장점이 있습니다.
- 자바에서 제공해주는 만큼 JVM이 내부적으로 모니터링을 처리하므로 개발자가 명시적으로 락을 관리할 필요가 없다는 장점이 있습니다.
하지만 동기화된 메서드는 한번에 하나의 스레드만 접근할 수 있기 때문에, 경쟁 상태가 심해지면 블로킹이 발생하거나, 데드락이 발생하며 성능이 크게 저하될 수 있습니다.

### 2) ReentrantLock + concurrentHashMap
> concurrentHashMap?
> 멀티스레드 환경에서 안전하게 데이터를 저장/조회할 수 있도록 설계된 Map
> 여러 스레드가 동일한 Map에 동시에 접근하여 읽거나 쓸 때 HashMap은 데이터 불일치, 경쟁조건이 발생 할 수 있음
> oncurrentHashMap은 내부적으로 segment 단위로 동기화를 처리하여 높은 성능을 제공.
> (한 번에 하나의 스레드만 특정 데이터에 접근하거나, 동시성 이슈가 발생하는 부분이 특정 로직 일부인 경우 ReentrantLock만 사용해도 가능함)

Lock은 자바5부터 도입된 인터페이스로 Lock의 구현체 중 ReentrantLock은 명시적으로 락을 관리할 수 있는 기능을 제공 해 syncronized와 유사하지만 더 세밀한 제어가 가능합니다.
```
Lock lock = new ReentrantLock();
lock.lock();
try{
 // 수행 할 로직 작성
catch(Exception e){
 // 에러 처리
}finally{
  lock.unlock();
}
```
- 명시적으로 제어할 수 있는 Lock은, 보다 유연한 제어가 가능하고 tryLock()메서드를 사용해 락을 얻지 못했을 때 블로킹 없이 대기할 수 있습니다.
- Lock은 Condition 인터페이스를 통해 보다 복잡한 동기화 및 대기/알림 메커니즘을 제공합니다.
하지만 락을 직접 관리해야 하므로 unlock()을 호출하지 않으면 리소스가 차단될 위험이 있고, 코드가 상대적으로 복잡해진다는 단점이 있습니다.

**해당 프로젝트에서는 ReentrantLock + concurrentHashMap을 사용하여 특정 키(userId)로 Lock을 관리**

### 3) Automic 변수
Automic 변수는 java.util.concurrent.atomic 패키지에서 제공하는 클래스들로,
원자적 연산을 지원하여 멀티스레드 환경에서 경쟁 상태 없이 안전하게 변수 값을 수정할 수 있도록 돕습니다. (ex. AutomicInteger, AutomicReference ..)

```
AtomicInteger atomicInt = new AtmoicInteger(0);
automicInt.incrementAndGet();
```
- 내부적으로 [CAS(Compare-And-Swap)](https://velog.io/@everysoon/CAS-Compare-And-Swap-with-Automic-%EB%B3%80%EC%88%98) 방식으로 원자적 연산을 수행하여 동시성 문제를 해결합니다.
- 별도의 락을 사용하지 않으므로 성능이 우수하고 따로 Lock을 관리하지않아도 됩니다. 특히 짧은 시간안에 값을 수정해야 하는 경우 유리합니다.
하지만 Automic 변수는 기본적인 연산(증가,감소,교환)만 지원하며, 복잡한 연산에는 적합하지 않습니다.
또, 하나의 변수에 대해서만 동시성 제어가 가능하므로 여러 변수간의 복잡한 상호작용이 필요한 경우에는 사용이 어렵습니다.

### 4) 정리
- syncronized : 간단한 동기화가 필요한 경우 (코드 간결성)
- Lock + concurrentHashMap: 복잡한 동기화가 필요한 경우, 여러 조건에 따라 대기/알람이 필요한 경우
- Automic 변수 : 간단한 변수 연산이 필요할 때 (ex. 카운터, 플래그 등)

**해당 READMD.d는 [자바를 이용한 단일 환경에서의 동시성 제어 방법](https://velog.io/@everysoon/%EC%9E%90%EB%B0%94%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-%EB%8B%A8%EC%9D%BC-%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C%EC%9D%98-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%A0%9C%EC%96%B4-%EB%B0%A9%EB%B2%95)을 요약한 것입니다.** 

#### 마치며
위의 세 가지 방법들은 단일 환경(단일 JVM)에서 동시성 제어에 주로 적합하고,
분산 환경에서는 여러 JVM 인스턴스가 네트워크를 통해 서로 통신하고 데이터를 공유하기 때문에, 락과 같은 동시성 제어를 관리하는 것이 더욱 어렵습니다.
분산 환경에서의 동시성 제어는 데이터 베이스나 메세지큐, 캐시 시스템(ex.Redis)등과 같은 외부 시스템을 활용하여 처리한다고 합니다.
[분산환경에서의 동시성제어를 위한 여러가지 방법들](https://velog.io/@everysoon/%EB%B6%84%EC%82%B0%ED%99%98%EA%B2%BD%EC%97%90%EC%84%9C%EC%9D%98-%EB%8F%99%EC%8B%9C%EC%84%B1%EC%A0%9C%EC%96%B4%EB%A5%BC-%EC%9C%84%ED%95%9C-%EC%97%AC%EB%9F%AC%EA%B0%80%EC%A7%80-%EB%B0%A9%EB%B2%95%EB%93%A4-3knonfxn)도 참고해보면 좋을 것 같습니다.


