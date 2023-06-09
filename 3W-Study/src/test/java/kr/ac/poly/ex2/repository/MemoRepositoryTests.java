/*  Entity Package에서 만들어 둔 Memo 테이블을 테스트 하는 Class
*   CRUD 테스트 */
package kr.ac.poly.ex2.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import kr.ac.poly.ex2.entity.Memo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
public class MemoRepositoryTests {
    @Autowired
    MemoRepository memoRepository;

    @Test
    /*  Insert Test
    *   memoText는 현재 Not Null 조건이기에, 데이터 삽입이 무조건 이루어져야한다. */
    public void testInsertDummies() {
        /* Lambda 식 표현으로 100개의 샘플 데이터 INSERT */
        IntStream.rangeClosed(1,100).forEach(i -> {
            Memo memo = Memo.builder().memoText("Sample : " + i).build(); // memoText 메소드 = 필드 값 부여
            memoRepository.save(memo);
        });
    }
    /*  Select Test
    *   findById() & get() 메소드를 통해 Entity 객체 조회가 가능하다. */
    @Test
    public void testSelect() {
        int mno = 50; // 찾으려는 행 Value
        Optional<Memo> selectData = memoRepository.findById(mno); /* Entity 값 가져오기 */
        System.out.println("==================================");

        /* SELECT 하려는 필드가 있는지 체크 후 출력. */
        if(selectData.isPresent()) {
            Memo memo = selectData.get(); // mno 필드의 값 가져오기.
            System.out.println(memo);
        }
    }
    /*  Transactional 어노테이션을 이용해 다른 방식으로 표현.
    * */
    @Transactional
    @Test
    public void transactionTestSelect() {
        int mno = 99;
        Memo memo = memoRepository.getOne(mno);
        System.out.println("==================================");
        /* SELECT 하려는 필드가 있는지 체크 후 출력. */
        System.out.println(memo);
    }

    /* UPDATE 테스트
    * CREATE 작업과 동일하게 save()를 이용한다. */
    @Test
    public void testUpdate() {
        // 100번 데이터에 있는 memoText 값 업데이트
        Memo memo = Memo.builder().mno(100).memoText("Check Update").build();
        System.out.println(memoRepository.save(memo)); // 업데이트 된 값 체크
    }

    /* DELETE 테스트
    *  상위 작업들과 동일하게, 해당 번호가 있는지 조회 후 삭제한다. */
    @Test
    public void testDelete() {
        int mno = 100; // 번호 100번의 데이터 삭제 지정.
        memoRepository.deleteById(mno);
    }

    /* 페이징 처리 테스트 */
    @Test
    public void testPageDefault() {
        /* 1 페이지에 10개 표시 */
        Pageable pageable = PageRequest.of(0,10);
        Page<Memo> result = memoRepository.findAll(pageable);
        System.out.println(result); // 결과 체크
        /* 테스트 코드 및 메소드 몇 가지 테스트 */
        System.out.println("-------------------");
        System.out.println("Total Pages : " + result.getTotalPages());  // 페이지의 총 개수
        System.out.println("Total Count : " + result.getTotalElements()); // 개체의 총 개수
        System.out.println("Page Number : " + result.getNumber()); // 현재 페이지의 번호
        System.out.println("Page Size : " + result.getSize()); // 한 페이지 당 개체(데이터)의 개수
        System.out.println("Has next Page ? : " + result.hasNext()); // 다음 페이지의 존재 여부(T or F)
        System.out.println("This Page is First ? : " + result.isFirst()); // 현재 페이지의 시작 페이지 확인(T or F)
        System.out.println("-------------------");
        /* getContent를 통해 실제 페이지 내에 테이블 값들을 가져오기. */
        for(Memo memo : result.getContent()) {
            System.out.println("| 현재 페이지 번호 : "+ result.getNumber() + " | 페이지 내부 값 | " + memo);
        }
    }

    /* 페이징 처리 Sort
    * 오름차순(ASC) & 내림차순(DESC) */
    @Test
    public void testSortPage() {
        // mno 컬럼을 기준으로 내림차순 정렬한다.
        Sort sort = Sort.by("mno").descending();
        // 페이지 0번의 크기 10개 & 내림차순으로 정렬
        Pageable pageable = PageRequest.of(0,10,sort);
        /*  제네릭 타입 : Memo 테이블
        *   파라미터 값을 pageable로 전달받아 한 페이지 내부의 값들을 전달한다. */
        Page<Memo> result = memoRepository.findAll(pageable);

        // 내림차순으로 정렬된 데이터 전부 출력
        result.get().forEach(memo -> {
            System.out.println(memo);
        });
    }

    /** 쿼리문 테스트 메소드
     *  내림차순으로 정렬하여 검색 */
    @Test
    public void testQueryMethods() {
        List<Memo> list = memoRepository.findByMnoBetweenOrderByMnoDesc(70,80);
        for(Memo memo : list) {
            System.out.println(memo);
        }
    }

    /** 쿼리 메소드와 Pageable를 결합하여 목록 생성하여 특정 필드 값부터 원하는 값까지 출력해준다. */
    @Test
    public void testQueryMethodWithPageable() {
        Pageable pageable = PageRequest.of(0,15, Sort.by("mno").descending());
        Page<Memo> result = memoRepository.findByMnoBetween(10,35, pageable);
        result.get().forEach(memo -> System.out.println(memo));
    }

    /** Transactional & Commit 어노테이션으로 Select 문 및
     *   10보다 작은 값 컬럼 값을 삭제한다.
     *   @Commit은 최종 결과를 커밋하기 위해 사용한다. */
    @Commit
    @Transactional
    @Test
    public void testDeleteQueryMethods() {
    memoRepository.deleteMemoByMnoLessThan(10);
    }

    /** MemoRepository에서 @Query 어노테이션을 통해 만든
     *  쿼리 메소드를 통해 내림차순으로 정렬해 출력 결과를 보여주는 메소드 */
    @Test
    public void testGetListDesc() {
        List<Memo> list = memoRepository.getListDesc();
        for(Memo memo : list) {
            System.out.println(memo);
        }
    }

    @Test
    public void testUpdateMemoText() {
        int updateCnt = memoRepository.updateMemoText(29, "파라미터 바인딩을 이용해 29행 수정");
    }

    @Test
    public void testUpdateMemoText2() {
        Memo memo = new Memo();
        memo.setMno(31);
        memo.setMemoText("[31행 수정]파라미터 바인딩을 이용하여 Memo객체 참조 값을 param으로 사용");
        int updateCnt = memoRepository.updateMemoText2(memo);
    }

    /** 파라미터 값보다 큰 컬럼들을 페이지의 크기(10)까지 출력해주는 테스트 쿼리 메소드 */
    @Test
    public void testGetListQuery() {
        /* Pageable = 페이지를 만든다. 현재 페이지 = 0번 째 , Size = 10 , 오름차순으로 정렬.
        * */
        Pageable pageable = PageRequest.of(0,10
                , Sort.by("mno").descending());
        Page<Memo> result = memoRepository.getListWithQuery(30, pageable);
        result.get().forEach(
                memo -> System.out.println(memo)
        );
    }

    @Test
    public void testGetListWithQueryObject() {
        Pageable pageable = PageRequest.of(0,10,Sort.by("mno").ascending());
        Page<Object[]> result = memoRepository.getListWithQueryObject(15,pageable);
        result.get().forEach(
                memo -> System.out.println(result)
        );
    }

    @Test
    public void nativeSQLTest() {
        List<Object[]> list = memoRepository.getNativeResult();
        for(Object[] obj : list) {
            System.out.println(obj[0] + " :: " + obj[1]);
        }
    }

    @Test
    public void deleteColumn() {
        for(int i = 50; i <= 300; i++) {
            memoRepository.deleteById(i);
        }
    }
}
