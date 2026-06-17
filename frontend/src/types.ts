export type RoleCode = 'ADMIN' | 'TEACHER' | 'ASSISTANT' | 'STUDENT'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface CurrentUser {
  id: number
  username: string
  realName: string
  roles: RoleCode[]
}

export interface AuthResponse {
  token: string
  user: CurrentUser
}

export interface UserSummary {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  status: number
  roles: RoleCode[]
  createdAt?: string
}

export interface OperationLog {
  id: number
  userId?: number
  username?: string
  operation: string
  targetType?: string
  targetId?: number
  ip?: string
  result: string
  detail?: string
  createdAt: string
}

export interface MenuItem {
  path: string
  title: string
  icon: string
  roles: RoleCode[]
}

export interface CourseApplication {
  id: number
  teacherId: number
  teacherName: string
  courseName: string
  courseCode: string
  description?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  rejectReason?: string
  reviewedBy?: number
  reviewedByName?: string
  reviewedAt?: string
  createdAt: string
}

export interface Course {
  id: number
  courseName: string
  courseCode: string
  description?: string
  ownerId: number
  ownerName: string
  memberRole: string
  status: string
  createdAt: string
}

export interface CourseDetailSummary {
  course: Course
  myRole: string
  memberCount: number
  assignmentCount: number
  submissionCount: number
  pendingSubmissionCount: number
  pendingGradingCount: number
  duplicateCount: number
  averageScore: number
  submissionRate: number
}

export interface CourseMember {
  id: number
  courseId: number
  userId: number
  username: string
  realName: string
  memberRole: string
  status: number
  joinedAt: string
}

export type AssignmentType = 'TEXT' | 'FILE' | 'SINGLE_CHOICE' | 'TRUE_FALSE'

export interface Assignment {
  id: number
  courseId: number
  courseName: string
  title: string
  description?: string
  assignmentType: AssignmentType
  deadline: string
  totalScore: number
  allowLate: boolean
  status: string
  createdBy: number
  createdByName: string
  createdAt: string
}

export interface Question {
  id: number
  assignmentId: number
  questionType: 'SINGLE_CHOICE' | 'TRUE_FALSE'
  content: string
  options?: string
  standardAnswer: string
  score: number
}

export interface Notification {
  id: number
  userId: number
  receiverId?: number
  title: string
  content: string
  type: string
  targetType?: string
  targetId?: number
  courseId?: number
  read: number
  readStatus?: number
  createdAt: string
}

export interface FileRecord {
  id: number
  submissionId: number
  fileName: string
  filePath: string
  fileSize: number
  fileHash: string
  uploadedAt: string
}

export interface AssignmentFile {
  id: number
  assignmentId: number
  fileName: string
  fileSize: number
  fileHash: string
  uploadedBy: number
  uploadedByName: string
  uploadedAt: string
}

export interface Submission {
  id: number
  assignmentId: number
  assignmentTitle: string
  assignmentType: AssignmentType
  courseId: number
  courseName: string
  studentId: number
  studentUsername: string
  studentName: string
  content?: string
  submitTime: string
  late: boolean
  status: string
  autoScore?: number
  finalScore?: number
  suspectedDuplicate: boolean
  hasSimilarityAlert?: boolean
  similarityScore?: number
  fileCount: number
}

export interface SubmissionDetail extends Submission {
  files: FileRecord[]
}

export interface StudentSubmissionAggregate {
  submissionId: number
  assignmentId: number
  assignmentTitle: string
  assignmentType: AssignmentType
  courseId: number
  courseName: string
  submitTime: string
  deadline: string
  late: boolean
  duplicate: boolean
  similarityAlert?: boolean
  similarityScore?: number
  autoScore?: number
  finalScore?: number
  gradeStatus?: string
  comment?: string
  fileName?: string
  fileHash?: string
  status: string
}

export interface DuplicateRecord {
  id: number
  assignmentId: number
  assignmentTitle: string
  fileHash: string
  submissionIds: string
  submissionIdList: number[]
  sourceSubmissionId?: number
  matchedSubmissionId?: number
  sourceStudentId?: number
  matchedStudentId?: number
  sourceStudentName?: string
  matchedStudentName?: string
  detectionType?: string
  similarityScore?: number
  threshold?: number
  detectedAt: string
  status: string
  remark?: string
}

export interface GradingItem {
  submissionId: number
  assignmentId: number
  assignmentTitle: string
  assignmentType: AssignmentType
  courseId: number
  courseName: string
  studentId: number
  studentUsername: string
  studentName: string
  submitTime: string
  late: boolean
  submissionStatus: string
  autoScore?: number
  finalScore?: number
  suspectedDuplicate: boolean
  hasSimilarityAlert?: boolean
  similarityScore?: number
  gradeId?: number
  graderId?: number
  graderName?: string
  gradeScore?: number
  comment?: string
  gradedAt?: string
  gradeStatus: string
  fileCount: number
}

export interface GradingDetail extends GradingItem {
  content?: string
  files: FileRecord[]
}

export interface GradingProgress {
  courseId: number
  courseName: string
  assignmentCount: number
  submissionCount: number
  pendingCount: number
  autoGradedCount: number
  gradedCount: number
  returnedCount: number
  gradingRate: number
}

export interface CourseStatistics {
  courseId: number
  courseName: string
  studentCount: number
  assignmentCount: number
  submissionCount: number
  submissionRate: number
  lateCount: number
  lateRate: number
  averageScore: number
  highestScore: number
  lowestScore: number
  duplicateCount: number
}

export interface ScoreBucket {
  label: string
  count: number
}

export interface AssignmentStatistics {
  assignmentId: number
  assignmentTitle: string
  courseId: number
  courseName: string
  studentCount: number
  submissionCount: number
  submissionRate: number
  lateCount: number
  lateRate: number
  averageScore: number
  highestScore: number
  lowestScore: number
  duplicateCount: number
  scoreDistribution: ScoreBucket[]
}

export interface GradeExportRow {
  studentNo: string
  studentName: string
  courseName: string
  assignmentName: string
  submitTime: string
  late: boolean
  suspectedDuplicate: boolean
  autoScore?: number
  manualScore?: number
  finalScore?: number
  comment?: string
  graderName?: string
}

export interface DashboardData {
  primaryRole: RoleCode
  generatedAt: string
  pendingCourseApplications?: number
  approvedCourseApplications?: number
  userCount?: number
  teacherCount?: number
  studentCount?: number
  assistantCount?: number
  myCourseCount?: number
  assignmentCount?: number
  pendingAssignmentCount?: number
  submittedAssignmentCount?: number
  pendingGradingCount?: number
  gradedCount?: number
  duplicateCount?: number
  unreadNotificationCount?: number
  recentApplications: CourseApplication[]
  recentCourses: Course[]
  recentLogs: OperationLog[]
  recentNotifications: Notification[]
}
