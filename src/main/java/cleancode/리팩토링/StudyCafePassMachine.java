package cleancode.리팩토링;

import cleancode.리팩토링.exception.AppException;
import cleancode.리팩토링.io.InputHandler;
import cleancode.리팩토링.io.OutputHandler;
import cleancode.리팩토링.io.StudyCafeFileHandler;
import cleancode.리팩토링.model.StudyCafeLockerPass;
import cleancode.리팩토링.model.StudyCafePass;
import cleancode.리팩토링.model.StudyCafePassType;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public class StudyCafePassMachine {

    private final InputHandler inputHandler = new InputHandler();
    private final OutputHandler outputHandler = new OutputHandler();
    StudyCafeFileHandler studyCafeFileHandler = new StudyCafeFileHandler();

    public void run() {
        try {
            outputHandler.showWelcomeMessage();                           //출력 : 스터디카페 안내
            outputHandler.showAnnouncement();                             //출력 : 스터디카페 상세안내

            StudyCafePass selectedPass = selectedPass();                  //이용권 선택

            Optional<StudyCafeLockerPass> optionalLockerPass = selectLockerPass(selectedPass);  //LockerPass 정책을 가져옴

            optionalLockerPass.ifPresentOrElse(
                    lockerPass -> outputHandler.showPassOrderSummary(selectedPass, lockerPass),   //if not null
                    () ->  outputHandler.showPassOrderSummary(selectedPass)        //if null
            );
            //if(optionalLockerPass.isPresent()) {
            //    outputHandler.showPassOrderSummary(selectedPass, optionalLockerPass.get());  //선택한 이용권 출력
            //} else{
            //    outputHandler.showPassOrderSummary(selectedPass, null);  //선택한 이용권 출력
            //}

        } catch (AppException e) {
            outputHandler.showSimpleMessage(e.getMessage());
        } catch (Exception e) {
            outputHandler.showSimpleMessage("알 수 없는 오류가 발생했습니다.");
        }
    }

    private StudyCafePass selectedPass() {
        outputHandler.askPassTypeSelection();                         //출력 : 시간(자유석|1), 주단위(자유석|2), 고정권(3)
        StudyCafePassType studyCafePassType = inputHandler.getPassTypeSelectingUserAction();   //입력 : 이용권 (ex, 시간권)

        List<StudyCafePass> passCandidates = findPassCandidatesBy(studyCafePassType);

        outputHandler.showPassListForSelection(passCandidates);                   //출력 : 이용권 목록 출력 (ex, 1. 2시간권 - 4000원)
        StudyCafePass selectedPass = inputHandler.getSelectPass(passCandidates);   //입력 ex : 시간권 리스트에서 입력한 index(숫자-1)에 해당되는 시간권 객체중에 입력
        return selectedPass;
    }

    private List<StudyCafePass> findPassCandidatesBy(StudyCafePassType studyCafePassType) {
        //중복된 지역 코드들을 전역화
        List<StudyCafePass> appPasses = studyCafeFileHandler.readStudyCafePasses();   //모든 이용권을 List<객체>화

        //중복된 지역 코드들을 전역화
        return appPasses.stream()   //hourlyPasses
                .filter(studyCafePass -> studyCafePass.getPassType() == studyCafePassType)   //StudyCafePassType.HOURLY => studyCafePassType
                .toList();     //ex) '시간' 단위 이용권 객체들만 추출
    }


    private Optional<StudyCafeLockerPass> selectLockerPass(StudyCafePass selectedPass) {
        if(selectedPass.getPassType() != StudyCafePassType.FIXED) {
            return Optional.empty();
        }

        StudyCafeLockerPass lockerPassCandidate = findLockerPassCandidateBy(selectedPass);


        boolean lockerSelection = false;
        if (lockerPassCandidate != null) {
            outputHandler.askLockerPass(lockerPassCandidate);         //출력 : 사물함 이용 여부
            lockerSelection = inputHandler.getLockerSelection();      //입력 : 사물함 선택 (1)

            if (lockerSelection) {     //사물함을 선택(1)했다면
                return Optional.of(lockerPassCandidate);
                //outputHandler.showPassOrderSummary(selectedPass, lockerPass);
            }

        }

        return Optional.empty();
    }


    private StudyCafeLockerPass findLockerPassCandidateBy(StudyCafePass pass) {
        List<StudyCafeLockerPass> allLockerPasses = studyCafeFileHandler.readLockerPasses();       //사물함 csv 파일 읽어오기

        return allLockerPasses.stream()
                .filter(locakerPass ->
                        locakerPass.getPassType() == pass.getPassType()
                                && locakerPass.getDuration() == pass.getDuration()
                )           //FIXED (고정권) 이용권 객체만 추출
                .findFirst()
                .orElse(null);
    }

}
