rpackage com.oasystem.attendance.service;

import com.oasystem.attendance.entity.Attendance;
import com.oasystem.attendance.entity.LeaveRequest;

import java.util.List;

public interface AttendanceService {
    void signIn(Long userId, String location);

    void signOut(Long userId, String location);

    List<Attendance> getRecords(Long userId, String month);

    Long applyLeave(LeaveRequest req);
}

